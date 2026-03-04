#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Collect Vue3 + Spring Boot 3 project structure & key source files into a compact bundle.
Usage:
  python collect_project_context.py --root . --out project_context.zip
"""

import argparse
import fnmatch
import os
import re
import sys
import zipfile
from datetime import datetime

DEFAULT_EXCLUDE_DIRS = {
    ".git", ".idea", ".vscode", ".gradle", ".mvn", ".settings",
    "node_modules", "dist", "build", "out",
    "target", "logs", "log", "tmp", "temp", ".cache",
}

DEFAULT_EXCLUDE_FILES_GLOBS = [
    "*.log", "*.tmp", "*.swp", "*.swo", "*.DS_Store",
    ".env", ".env.*", "*.keystore", "*.jks", "*.p12",
    "pnpm-lock.yaml", "yarn.lock", "package-lock.json",  # 依赖锁可选：你也可以保留
    "*.iml",
]

# 允许采集的文本后缀（避免打包二进制）
TEXT_EXTS = {
    ".md", ".txt", ".json", ".yaml", ".yml", ".xml", ".properties", ".conf",
    ".js", ".ts", ".tsx", ".vue", ".css", ".scss", ".less",
    ".java", ".kt", ".kts", ".gradle",
    ".sql", ".sh", ".bat",
}

# 对“关键文件”的优先采集模式（更快理解结构）
KEY_FILE_GLOBS = [
    # Vue / Vite
    "package.json",
    "vite.config.*",
    "vitest.config.*",
    "tsconfig*.json",
    "jsconfig*.json",
    "index.html",
    "src/main.*",
    "src/App.vue",
    "src/router/**",
    "src/store/**",
    "src/stores/**",
    "src/api/**",
    "src/services/**",
    "src/utils/**",
    "src/components/**",
    "src/views/**",
    "src/pages/**",
    "src/layouts/**",
    "src/plugins/**",
    "src/composables/**",
    "src/hooks/**",

    # Spring Boot / Maven / Gradle
    "pom.xml",
    "build.gradle",
    "build.gradle.kts",
    "settings.gradle",
    "settings.gradle.kts",
    "gradlew",
    "gradlew.bat",
    "mvnw",
    "mvnw.cmd",
    "src/main/resources/application*.yml",
    "src/main/resources/application*.yaml",
    "src/main/resources/application*.properties",
    "src/main/java/**",
    "src/main/kotlin/**",
    "src/test/**",
    "src/main/resources/mapper/**",
    "src/main/resources/db/**",
    "src/main/resources/sql/**",
]

def is_excluded_path(rel_path: str, exclude_dirs, exclude_globs) -> bool:
    parts = rel_path.split(os.sep)
    if any(p in exclude_dirs for p in parts):
        return True
    base = os.path.basename(rel_path)
    for g in exclude_globs:
        if fnmatch.fnmatch(base, g):
            return True
    return False

def is_text_file(path: str) -> bool:
    ext = os.path.splitext(path)[1].lower()
    if ext in TEXT_EXTS:
        return True
    # 没后缀但可能是脚本/配置
    base = os.path.basename(path)
    if base in {"Dockerfile", "Makefile"}:
        return True
    return False

def glob_match(path: str, pattern: str) -> bool:
    # 支持 ** 的简单匹配
    # 将 pattern 转换为正则
    # 例如 src/router/** -> ^src/router/.*$
    p = pattern.replace("\\", "/")
    s = path.replace("\\", "/")
    p = re.escape(p).replace(r"\*\*", ".*").replace(r"\*", "[^/]*")
    return re.match("^" + p + "$", s) is not None

def build_tree(root: str, exclude_dirs, exclude_globs, max_depth: int = 8) -> str:
    lines = []
    root = os.path.abspath(root)
    for cur_dir, dirnames, filenames in os.walk(root):
        rel_dir = os.path.relpath(cur_dir, root)
        if rel_dir == ".":
            rel_dir = ""
        if rel_dir and is_excluded_path(rel_dir, exclude_dirs, exclude_globs):
            dirnames[:] = []
            continue

        depth = 0 if not rel_dir else rel_dir.count(os.sep) + 1
        if depth > max_depth:
            dirnames[:] = []
            continue

        # 排除目录（原地修改，阻止继续 walk）
        dirnames[:] = [d for d in dirnames if not is_excluded_path(os.path.join(rel_dir, d), exclude_dirs, exclude_globs)]
        filenames = [f for f in filenames if not is_excluded_path(os.path.join(rel_dir, f), exclude_dirs, exclude_globs)]

        indent = "  " * depth
        display = rel_dir if rel_dir else "."
        lines.append(f"{indent}{display}/")
        for f in sorted(filenames)[:200]:
            lines.append(f"{indent}  {f}")
        if len(filenames) > 200:
            lines.append(f"{indent}  ... ({len(filenames)-200} more files)")
    return "\n".join(lines)

def collect_files(root: str, exclude_dirs, exclude_globs, include_mode: str):
    """
    include_mode:
      - "key": only key files matched by KEY_FILE_GLOBS
      - "all": all text files (still excluding big dirs/globs)
    """
    root = os.path.abspath(root)
    collected = []
    for cur_dir, dirnames, filenames in os.walk(root):
        rel_dir = os.path.relpath(cur_dir, root)
        if rel_dir == ".":
            rel_dir = ""
        if rel_dir and is_excluded_path(rel_dir, exclude_dirs, exclude_globs):
            dirnames[:] = []
            continue
        dirnames[:] = [d for d in dirnames if not is_excluded_path(os.path.join(rel_dir, d), exclude_dirs, exclude_globs)]

        for f in filenames:
            rel_path = os.path.join(rel_dir, f) if rel_dir else f
            if is_excluded_path(rel_path, exclude_dirs, exclude_globs):
                continue
            abs_path = os.path.join(root, rel_path)
            if include_mode == "all":
                if is_text_file(abs_path):
                    collected.append(rel_path)
            else:
                # key mode
                for pat in KEY_FILE_GLOBS:
                    if glob_match(rel_path, pat):
                        if is_text_file(abs_path) or os.path.basename(rel_path) in {"gradlew", "gradlew.bat", "mvnw", "mvnw.cmd"}:
                            collected.append(rel_path)
                        break
    # 去重 & 排序
    return sorted(set(collected))

def write_zip(root: str, out_zip: str, files: list, tree_text: str):
    root = os.path.abspath(root)
    with zipfile.ZipFile(out_zip, "w", compression=zipfile.ZIP_DEFLATED) as z:
        # 写目录树
        meta = [
            f"Collected at: {datetime.now().isoformat(timespec='seconds')}",
            f"Root: {root}",
            f"Files: {len(files)}",
            "",
            "==== TREE ====",
            tree_text,
            "",
        ]
        z.writestr("PROJECT_TREE.txt", "\n".join(meta))

        for rel_path in files:
            abs_path = os.path.join(root, rel_path)
            try:
                # 避免特别大的文本文件
                size = os.path.getsize(abs_path)
                if size > 2 * 1024 * 1024:
                    # 2MB+ 的文件改为截断写入
                    with open(abs_path, "rb") as f:
                        data = f.read(200 * 1024)  # 200KB
                    z.writestr(rel_path + ".TRUNCATED.txt", data.decode("utf-8", errors="replace") + "\n\n[TRUNCATED]")
                    continue

                z.write(abs_path, arcname=rel_path)
            except Exception as e:
                z.writestr(rel_path + ".ERROR.txt", f"Failed to read {rel_path}: {e}")

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--root", default=".", help="Project root path")
    ap.add_argument("--out", default="project_context.zip", help="Output zip filename")
    ap.add_argument("--mode", choices=["key", "all"], default="key",
                    help="key: only key files; all: all text files (larger)")
    ap.add_argument("--max-depth", type=int, default=8, help="Max depth for directory tree")
    args = ap.parse_args()

    root = os.path.abspath(args.root)
    if not os.path.isdir(root):
        print(f"Root is not a directory: {root}", file=sys.stderr)
        sys.exit(2)

    tree_text = build_tree(root, DEFAULT_EXCLUDE_DIRS, DEFAULT_EXCLUDE_FILES_GLOBS, max_depth=args.max_depth)
    files = collect_files(root, DEFAULT_EXCLUDE_DIRS, DEFAULT_EXCLUDE_FILES_GLOBS, include_mode=args.mode)

    # 额外补充：把 README / docs 也尽量带上（如果是文本）
    for extra in ["README.md", "readme.md", "docs/README.md"]:
        p = os.path.join(root, extra)
        if os.path.isfile(p) and extra not in files:
            files.append(extra)
    files = sorted(set(files))

    out_zip = os.path.abspath(args.out)
    write_zip(root, out_zip, files, tree_text)
    print(f"[OK] Written: {out_zip}")
    print(f"[OK] Files collected: {len(files)}")
    print("You can upload the zip or paste PROJECT_TREE.txt content here.")

if __name__ == "__main__":
    main()
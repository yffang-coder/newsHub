import argparse
import os
import pathlib
import shutil
import subprocess
import sys
import tarfile
import tempfile
from typing import Iterable


PROJECT_ROOT = pathlib.Path(__file__).resolve().parents[1]


def _run(cmd: list[str]) -> None:
    completed = subprocess.run(cmd, check=False)
    if completed.returncode != 0:
        raise SystemExit(completed.returncode)


def _ensure_executable_exists(name: str) -> None:
    if shutil.which(name) is None:
        raise SystemExit(f"Missing executable: {name}")


def _tar_add(tar: tarfile.TarFile, root: pathlib.Path, rel_path: pathlib.Path) -> None:
    abs_path = root / rel_path
    tar.add(abs_path, arcname=str(rel_path).replace("\\", "/"), recursive=True)


def _should_exclude(path: pathlib.Path, excludes: Iterable[str]) -> bool:
    normalized = str(path).replace("\\", "/")
    for pattern in excludes:
        if pathlib.PurePosixPath(normalized).match(pattern):
            return True
    return False


def build_archive(
    archive_path: pathlib.Path,
    root: pathlib.Path,
    excludes: list[str],
) -> None:
    with tarfile.open(archive_path, "w:gz") as tar:
        for p in root.rglob("*"):
            if p.is_dir():
                continue
            rel = p.relative_to(root)
            if _should_exclude(rel, excludes):
                continue
            _tar_add(tar, root, rel)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--host", required=True, help="SSH host, e.g. 101.43.43.228")
    parser.add_argument("--user", default="root", help="SSH user, default: root")
    parser.add_argument("--port", default="22", help="SSH port, default: 22")
    parser.add_argument(
        "--remote-dir",
        default="/www/wwwroot/blog",
        help="Remote directory to deploy into, default: /www/wwwroot/blog",
    )
    parser.add_argument(
        "--compose-dir",
        default="deployment",
        help="Relative directory containing docker-compose.yml, default: deployment",
    )
    parser.add_argument(
        "--no-build",
        action="store_true",
        help="Run docker-compose up without --build",
    )
    args = parser.parse_args()

    _ensure_executable_exists("ssh")
    _ensure_executable_exists("scp")

    ssh_target = f"{args.user}@{args.host}"
    ssh_base = ["ssh", "-p", str(args.port), ssh_target]
    scp_base = ["scp", "-P", str(args.port)]

    excludes = [
        ".git/**",
        "**/node_modules/**",
        "**/dist/**",
        "**/target/**",
        "**/*.log",
        "**/.DS_Store",
        "**/.idea/**",
        "**/.vscode/**",
        "**/*.iml",
        "deployment/logs/**",
        "backend/build_log.txt",
    ]

    with tempfile.TemporaryDirectory() as tmp:
        archive_path = pathlib.Path(tmp) / "newshub.tar.gz"
        build_archive(archive_path, PROJECT_ROOT, excludes)

        remote_tmp = f"/tmp/newshub-{os.getpid()}.tar.gz"
        _run([*scp_base, str(archive_path), f"{ssh_target}:{remote_tmp}"])

        build_flag = "0" if args.no_build else "1"
        remote_cmd = (
            f"set -e; "
            f"mkdir -p '{args.remote_dir}'; "
            f"cd '{args.remote_dir}'; "
            f"tar -xzf '{remote_tmp}'; "
            f"rm -f '{remote_tmp}'; "
            f"cd '{args.remote_dir}/{args.compose_dir}'; "
            f"chmod +x deploy.sh || true; "
            f"DEPLOY_BUILD='{build_flag}' ./deploy.sh; "
        )
        _run([*ssh_base, remote_cmd])


if __name__ == "__main__":
    main()

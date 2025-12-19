# UT-specific build instructions

* To build a new copy of this Docker image:
```
docker build \
    --build-arg GITHUB_USER=XXXXX \
    --build-arg GITHUB_TOKEN=YYYYYY -t laitsdev/zumult:builder-ZZZZZZZ .
```
XXXXX = your github username (see scripts/README.md)
YYYYY = your github token (see scripts/README.md)
ZZZZZ = current timestamp e.g. '20251231T1530'
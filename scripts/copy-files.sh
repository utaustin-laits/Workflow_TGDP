#!/usr/bin/bash

mc alias set local https://minio.la.utexas.edu "$S3_ACCESS_KEY" "$S3_SECRET_KEY"

while IFS= read -r interviewsectionid; do
    # Split on '-' and capture first three fields
    IFS='-' read -r f1 f2 f3 _ <<<"$interviewsectionid"

    prefix=""
    if [[ -n "${f1:-}" && -n "${f2:-}" && -n "${f3:-}" ]]; then
      prefix="$f1-$f2-$f3"
    fi

    echo "copying $interviewsectionid"
    mc cp --recursive local/speech-islands/interviews/$prefix/$interviewsectionid.eaf /work/TGDP/$prefix
    mc cp --recursive local/speech-islands/sound_files/$prefix/$interviewsectionid.mp3 /work/TGDP/$prefix
    mc cp --recursive local/speech-islands/sound_files/$prefix/$interviewsectionid.wav /work/TGDP/$prefix
done < "public-sections.txt"

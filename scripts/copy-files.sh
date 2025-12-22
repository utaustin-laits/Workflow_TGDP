#!/usr/bin/bash -e

mc alias set local https://minio.la.utexas.edu "$S3_ACCESS_KEY" "$S3_SECRET_KEY"

while IFS= read -r interviewsectionid; do
    # Split on '-' and capture first three fields
    IFS='-' read -r f1 f2 f3 _ <<<"$interviewsectionid"

    prefix=""
    if [[ -n "${f1:-}" && -n "${f2:-}" && -n "${f3:-}" ]]; then
      prefix="$f1-$f2-$f3"
    fi

    echo "copying $interviewsectionid"
    if [[ ! -f "/corpusdata/TGDP/$prefix/$interviewsectionid.eaf" ]]; then
      mc cp --recursive local/speech-islands/interviews/$prefix/$interviewsectionid.eaf /corpusdata/TGDP/$prefix
    fi
    if [[ ! -f "/corpusdata/TGDP/$prefix/$interviewsectionid.mp3" ]]; then
      mc cp --recursive local/speech-islands/sound_files/$prefix/$interviewsectionid.mp3 /corpusdata/TGDP/$prefix
    fi
    if [[ ! -f "/corpusdata/TGDP/$prefix/$interviewsectionid.wav" ]]; then
      mc cp --recursive local/speech-islands/sound_files/$prefix/$interviewsectionid.wav /corpusdata/TGDP/$prefix
    fi
done < "public-sections.txt"

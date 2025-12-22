# Archive creation

Periodically, a new archive needs to be created.  This folder contains instructions and scripts for doing that.

## Instructions

Hopefully temporary setup:
1. Log into Github.com with an account that has permissions to download from https://github.com/zumult-org/zumultapi
2. Go to https://github.com/settings/tokens and generate a new (classic) token, with `read:packages` permissions.
3. Set the `GITHUB_TOKEN` environment variable with this token value.
4. Set the `GITHUB_USER` environment variable with the username associated with that token.

Build instructions:
1. Update 'public-sections.txt' by running the SQL ```SELECT reference_identifier FROM `interview_sections` WHERE is_public=1 AND transcript_files_exist=1 AND sound_files_exist=1``` on the main Speech Islands database.  Strip out the header row and remove any quotes; format should match what's here.
2. Build and deploy a new copy of this workload to `zumult-builder`.  Delete the contents of the /lucene-indices and /corpusdata folders there.
3. Copy in new sound files by running `/scripts/copy-files.sh`.
4. Create the COMA file
   - Create the TGDP.coma file, by running `php artisan app:create-zumult-integration-files` in the 'web' container for Speech Islands.  (You can do this on the server, or in a local copy with a fresh database export.)
   - Save it to /corpusdata/TGDP/TGDP.coma
5. Run `/scripts/build.sh`
6. Build a new version of the `zumult` workload and deploy it.  Copy all files from /build/lucene-indices/SB_TGDP to /lucene-indices/SB_TGDP, and all files from /build/corpusdata/TGDP to /corpusdata/TGDP.
   - There should be existing 'SB_GTXG' and 'GTXG' subfolders in those two folders, respectively.  If not, grab a new copy from https://utexas.app.box.com/folder/350787458016
7. Test that Zumult is working correctly.  Shut down the `zumult-builder` workload.  Delete any temporary files you made during the deployment process.

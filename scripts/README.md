# Archive creation

Periodically, a new archive needs to be created.  This folder contains instructions and scripts for doing that.

## Instructions

1. Delete (or move aside) the contents of the /corpusdata and /lucene-indices folders in the 'zumult' workload.
2. Update 'public-sections.txt' by running the SQL ```SELECT reference_identifier FROM `interview_sections` WHERE is_public=1 AND transcript_files_exist=1``` on the main Speech Islands database.
    - FIXME: Once public server exists and is in the archive process happening before this, get this from the new public DB instead.
3. Update that workload with a newly-built version of this repo.
4. Copy in new sound files by running `copy-files.sh`.  Replace the Minio user/pass there temporarily first.
5. Run `delete-non-public.sh`. 
6. Create XML files from the EAFS by running `process-eafs.sh`.
   - Hopefully temporary: first update /usr/local/tomcat/webapps/ROOT/WEB-INF/web.xml to comment out the '/workflow' line and allow public access to that URL.
   - This process will take a few hours, so grab a snack or something 
7. Create the COMA file
   - Create the TGDP.coma file, by running `php artisan app:create-zumult-integration-files` in the 'web' container for Speech Islands.  (You can do this on the server, or in a local copy with a fresh database export.)
   - Save it to /corpusdata/TGDP/TGDP.coma 
8. Index the COMA file by running `/archive-creation/create-coma-index.sh`
9. Create the Lucene index by running `/archive-creation/make-lucene.sh`
10. Restart the app by redeploying the container. 
11. Load the app and test.

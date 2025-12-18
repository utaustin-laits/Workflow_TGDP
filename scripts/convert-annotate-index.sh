#!/bin/sh

# Unix shell script version of src/main/java/de/linguisticbits/workflow/ConvertAnnotateIndex.bat.
# If that changes, change this to match.

# Exit on error and on use of unset variables
set -eu

# Change to the directory where this script resides
# (POSIX-compliant replacement for %~dp0)
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
cd "$SCRIPT_DIR"

# The JAR compiled as target for Workflow_TGDP
WORKFLOW_JAR="/build/target/Workflow_TGDP-1.0-SNAPSHOT.jar"

# The directory containing all dependencies as JARs
# Adjust the path and wildcard as appropriate
LIB_DIRECTORY="/build/libs/*"

# Combined classpath (':' is the Unix classpath separator)
CLASS_PATH="$WORKFLOW_JAR:$LIB_DIRECTORY"

# The path to java (or just 'java' if it is on PATH)
JAVA_CMD="java"

echo "=========================================="
echo "Conversion / Annotation"
echo "=========================================="

"$JAVA_CMD" -classpath "$CLASS_PATH" \
  de.linguisticbits.workflow.ConvertAndAnnotate "$1"

echo "=========================================="
echo "Fix issues (if any) that would cause trouble with the indexer"
echo "=========================================="

"$JAVA_CMD" -classpath "$CLASS_PATH" \
  de.linguisticbits.workflow.indexing.FixMTASIndexingProblems "$1"

echo "=========================================="
echo "Indexing for MTAS / Annotation"
echo "=========================================="

"$JAVA_CMD" -classpath "$CLASS_PATH" \
  de.linguisticbits.workflow.indexing.IndexForMTAS "$2" "$3" "$4" "$1"

echo "=========================================="
echo "Indexing for IDs in COMA"
echo "=========================================="

"$JAVA_CMD" -classpath "$CLASS_PATH" \
  de.linguisticbits.workflow.indexing.IndexForCOMA "$1"

echo "=========================================="
echo "Stats for COMA"
echo "=========================================="

"$JAVA_CMD" -classpath "$CLASS_PATH" \
  de.linguisticbits.workflow.indexing.StatsForCOMA "$1"

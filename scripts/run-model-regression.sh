#!/usr/bin/env bash
#
# Runs the model regression tests (Maven profile "regression") in the five VEGA-models-* repositories, one after the
# other, and prints a per-repository summary of green and failed modules with their times.
#
# The repositories are looked up next to this one (../VEGA-models-*), or under the directory given with --root.
#
# Usage:
#   scripts/run-model-regression.sh [options] [-- extra Maven arguments]
#
# Options:
#   --dataset ts|core        run only one dataset (default: both)
#   --update                 regenerate the reference files (-Dgolden.update=true) instead of comparing
#   --core-version X.Y.Z     run against another InSilico Core version (-Dvega.core.version)
#   --no-adi                 do not compare the ADI on the core dataset (-Dregression.adi=false)
#   --repos "a b ..."        only these repositories, e.g. --repos "fate ecotox" (default: all five)
#   --root DIR               directory that contains the VEGA-models-* repositories
#   --online                 do not pass -o to Maven
#   -h, --help               show this help
#
# Exit status: 0 only if every module of every repository passed; 1 otherwise.

set -u

REPOS=(fate ecotox other properties human)

# Modules that do not compile for reasons unrelated to the regression tests: they are excluded from the build and
# listed in the summary, so the rest of the repository can still be checked.
#   ecotox: SDuckweedEC50Coral and HarlequinNOECCoral import insilico.core.coral.models.ec50, which Core 1.3.21 lacks.
declare -A BROKEN=(
    [ecotox]="VegaModels-SDuckweedEC50Coral VegaModels-HarlequinNOECCoral"
)

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
root="$(cd "$script_dir/../.." && pwd)"
mvn_args=()
offline=(-o)
selected=()

usage() { sed -n '2,/^$/p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'; }

while [ $# -gt 0 ]; do
    case "$1" in
        --dataset)      [ $# -ge 2 ] || { echo "--dataset needs a value" >&2; exit 2; }
                        case "$2" in ts|core) ;; *) echo "--dataset must be ts or core" >&2; exit 2;; esac
                        mvn_args+=("-Dregression.dataset=$2"); shift 2;;
        --update)       mvn_args+=("-Dgolden.update=true"); shift;;
        --core-version) [ $# -ge 2 ] || { echo "--core-version needs a value" >&2; exit 2; }
                        mvn_args+=("-Dvega.core.version=$2"); shift 2;;
        --no-adi)       mvn_args+=("-Dregression.adi=false"); shift;;
        --repos)        [ $# -ge 2 ] || { echo "--repos needs a value" >&2; exit 2; }
                        read -r -a selected <<< "$2"; shift 2;;
        --root)         [ $# -ge 2 ] || { echo "--root needs a value" >&2; exit 2; }
                        root="$(cd "$2" && pwd)" || exit 2; shift 2;;
        --online)       offline=(); shift;;
        -h|--help)      usage; exit 0;;
        --)             shift; mvn_args+=("$@"); break;;
        *)              echo "Unknown option: $1 (see --help)" >&2; exit 2;;
    esac
done
[ ${#selected[@]} -gt 0 ] && REPOS=("${selected[@]}")

log_dir="$(mktemp -d "${TMPDIR:-/tmp}/model-regression.XXXXXX")"
summary=()
failed_total=0
start_all=$(date +%s)

for name in "${REPOS[@]}"; do
    dir="$root/VEGA-models-$name"
    log="$log_dir/$name.log"
    if [ ! -f "$dir/pom.xml" ]; then
        summary+=("$(printf '%-11s  MISSING: %s' "$name" "$dir")")
        failed_total=$((failed_total + 1))
        continue
    fi

    exclude=()
    broken="${BROKEN[$name]:-}"
    if [ -n "$broken" ]; then
        exclude=(-pl "$(printf '!%s,' $broken | sed 's/,$//')")
    fi

    echo "=== VEGA-models-$name ($log)"
    start=$(date +%s)
    (cd "$dir" && mvn "${offline[@]}" -B test -Pregression -fae "${exclude[@]}" "${mvn_args[@]}") > "$log" 2>&1
    rc=$?
    elapsed=$(( $(date +%s) - start ))

    # One surefire line per test class (i.e. per module):
    #   "[INFO] Tests run: 2, Failures: 0, ... -- in XRegressionTest"              green
    #   "[WARNING] Tests run: 2, ... Skipped: 1, ... -- in XRegressionTest"          green, one dataset skipped
    #   "[ERROR] Tests run: 2, Failures: 1, ... <<< FAILURE! -- in XRegressionTest"  failed
    # A module skips its [ts] test when it overrides hasTrainingSet() to false (model with an empty training set).
    ok=$(grep -cE '^\[(INFO|WARNING)\] Tests run: .* -- in [A-Za-z0-9_.]*RegressionTest$' "$log")
    dataset_skipped=$(grep -cE '^\[WARNING\] Tests run: .*Skipped: [1-9].* -- in [A-Za-z0-9_.]*RegressionTest$' "$log")
    failed_tests=$(grep -E '^\[ERROR\] Tests run: .* -- in [A-Za-z0-9_.]*RegressionTest$' "$log" | sed -E 's/.* -- in //')
    # The reactor summary shows the project <name>, not the artifactId: it is used only for the modules that failed
    # before running their tests (e.g. compilation errors) or were skipped after an upstream failure.
    reactor_failed=$(grep -E '^\[INFO\] .* FAILURE \[' "$log" | sed -E 's/^\[INFO\] (.*[^ .]) *\.* FAILURE.*/\1/')
    reactor_skipped=$(grep -E '^\[INFO\] .* SKIPPED$' "$log" | sed -E 's/^\[INFO\] (.*[^ .]) *\.* SKIPPED$/\1/')
    ko=$(printf '%s' "$failed_tests" | grep -c .)
    rf=$(printf '%s' "$reactor_failed" | grep -c .)
    sk=$(printf '%s' "$reactor_skipped" | grep -c .)
    # A module whose test fails is also a reactor FAILURE: the build failures are the remaining ones.
    bf=$((rf > ko ? rf - ko : 0))
    # Test classes that left no result at all (e.g. a module pom with <skipTests>true</skipTests>, which overrides the
    # profile of the parent): they would otherwise go unnoticed. Excluded modules are not expected to report.
    reported=$(grep -E '^\[(INFO|WARNING|ERROR)\] Tests run: .* -- in [A-Za-z0-9_.]*RegressionTest$' "$log" \
        | sed -E 's/.* -- in //; s/.*\.//' | sort -u)
    expected=$(cd "$dir" && for t in VegaModels-*/src/test/java/*RegressionTest.java; do
        [ -f "$t" ] || continue
        case " $broken " in *" ${t%%/*} "*) continue;; esac
        basename "$t" .java
    done | sort -u)
    missing=$(comm -23 <(printf '%s\n' "$expected") <(printf '%s\n' "$reported") | grep .)
    ms=$(printf '%s' "$missing" | grep -c .)
    line=$(printf '%-11s  green %3d  failed %3d  build failures %2d  skipped %2d  no result %2d  %4dm%02ds' \
        "$name" "$ok" "$ko" "$bf" "$sk" "$ms" $((elapsed / 60)) $((elapsed % 60)))
    [ "$dataset_skipped" -gt 0 ] && line+="  ($dataset_skipped without a training set: ts skipped)"
    [ -n "$broken" ] && line+="  (excluded, do not compile: $broken)"
    summary+=("$line")
    while read -r m; do [ -n "$m" ] && summary+=("             FAILED  $m"); done <<< "$failed_tests"
    if [ "$bf" -gt 0 ]; then
        summary+=("             reactor failures (includes the failed tests above):")
        while read -r m; do [ -n "$m" ] && summary+=("               $m"); done <<< "$reactor_failed"
    fi
    while read -r m; do [ -n "$m" ] && summary+=("             SKIPPED $m"); done <<< "$reactor_skipped"
    while read -r m; do [ -n "$m" ] && summary+=("             NO RESULT $m"); done <<< "$missing"

    if [ $rc -ne 0 ] || [ "$ko" -gt 0 ] || [ "$rf" -gt 0 ] || [ "$sk" -gt 0 ] || [ "$ms" -gt 0 ]; then
        failed_total=$((failed_total + 1))
        [ $rc -ne 0 ] && [ "$ko" -eq 0 ] && [ "$rf" -eq 0 ] && [ "$sk" -eq 0 ] && summary+=("             Maven exited with $rc: see $log")
    fi
done

elapsed_all=$(( $(date +%s) - start_all ))
echo
echo "=== Model regression summary (logs in $log_dir)"
printf '%s\n' "${summary[@]}"
printf 'Total time: %dm%02ds\n' $((elapsed_all / 60)) $((elapsed_all % 60))

if [ $failed_total -gt 0 ]; then
    echo "RESULT: FAILED ($failed_total repositories with failures)"
    exit 1
fi
echo "RESULT: all green"
exit 0

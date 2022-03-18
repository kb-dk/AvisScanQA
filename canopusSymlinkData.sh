#!/usr/bin/env bash

set -e

cd ~/data

rm -rf -- *

find /avis-scanner-prod/_07-levering-fra-Ninestars -type d -print0 | xargs -r -i -0 mkdir -p "./{}"

find /avis-scanner-prod/_07-levering-fra-Ninestars -type f -print0 | xargs -r -i -0 ln -sf "{}" "./{}"
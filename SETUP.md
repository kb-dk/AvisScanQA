SETUP
- Kræver tomcat
- Kræver en postgresql database
- AvisScanQA_web/conf/avischk-db-schema.sql 
  -  skal køres for opsætning af database tabeller
- AvisScanQA_web/conf/AvisScanQA_web-behaviour.yaml
  - Connection string til database skal rettes
- AvisScanQA_web/conf/AvisScanQA_web-local.yaml
  - Kode til database skal rettes
- AvisScanQA_cli/src/main/package/conf/AvisScanQA_cli-behaviour.yaml
  - Connection string skal rettes.
  - numThreads kan rettes alt efter hvor mange resourcer der er til rådighed

- Der skal være læse/skrive adgang til filer som ligger her: \\thiele\from-9stars

CLI opsætning
- Der anvendes Java17
- Der skal installeres:
  - ImageMagick
  - Exiv2
- Hvis batches ikke har checksum filer skal createChecksumsTxt.sh køres på følgende måde:
  - cd \\thiele\from-9stars (Eller hvad end stien til mappen hvor batch mapperne er)
  - set -e; for dir in *_RT*; do echo "$dir"; pushd "$dir"; /x/x/x/createChecksumsTxt.sh;popd;done
    - x skal rettes til rette sti til createChecksumsTxt.sh
- For at lave QA køres
  - ./checkNewBatches.sh {Sti til hvor batch mapperne er}


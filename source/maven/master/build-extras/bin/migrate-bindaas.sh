OLD_BINDAAS=$1
SCRIPTDIR=$( cd -P -- "$(dirname -- "$(command -v -- "$0")")" && pwd -P )
NEW_BINDAAS=$SCRIPTDIR/../.
echo Importing settings and configurations from $OLD_BINDAAS
cp $OLD_BINDAAS/bin/*.json $NEW_BINDAAS/bin
cp $OLD_BINDAAS/bin/*.db $NEW_BINDAAS/bin
cp $OLD_BINDAAS/bin/bindaas.*.properties $NEW_BINDAAS/bin
cp $OLD_BINDAAS/bin/mailService.properties $NEW_BINDAAS/bin
cp -R $OLD_BINDAAS/bin/projects $NEW_BINDAAS/bin
echo Done!


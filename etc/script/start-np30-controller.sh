#!/bin/bash
#cd into directory where this script is located
cd "$(dirname "$0")"
exec java -jar np30-controller*.jar -Xmx128M

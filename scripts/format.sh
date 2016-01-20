#!/bin/sh

if ! command -v astyle >/dev/null 2>&1; then
    echo "astyle required, but couldn't be found."
    exit 1
fi

echo "Formating source files..."
# Modified kdelibs coding style as defined in
#   http://techbase.kde.org/Policies/Kdelibs_Coding_Style

find -regex ".*\.\(java\)" -exec \
    astyle --mode=java --indent=spaces=4 \
      --indent-labels --pad-oper --unpad-paren --pad-header \
      --keep-one-line-statements --convert-tabs \
      --indent-preprocessor "{}" \;
      
  echo "Done!"

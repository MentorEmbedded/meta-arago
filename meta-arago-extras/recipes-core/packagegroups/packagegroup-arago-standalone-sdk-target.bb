DESCRIPTION = "Target packages for the standalone SDK"
PR = "r13"
LICENSE = "MIT"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS_${PN} = "\
    libgcc \
    libgcc-dev \
    libstdc++-dev \
    libgomp-dev \
    ${LIBC_DEPENDENCIES} \
    libc-staticdev \
    linux-libc-headers-dev \
    gdbserver \
    alsa-dev \
    alsa-lib-dev \
    alsa-utils-dev \
    curl-dev \
    i2c-tools-dev \
    freetype-dev \
    ${@oe.utils.conditional('PREFERRED_PROVIDER_jpeg', 'libjpeg-turbo', 'libjpeg-turbo-dev', 'jpeg-dev', d)}  \
    lzo-dev \
    opkg-dev \
    libpng-dev \
    readline-dev \
    ${@oe.utils.conditional('QT_PROVIDER', 'qt5', '', 'tslib-dev', d)} \
    libusb-compat-dev \
    libusb1-dev \
    zlib-dev \
    ncurses-dev \
    opkg-dev \
    util-linux-dev \
"

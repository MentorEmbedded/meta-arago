DESCRIPTION = "Basic task to get a device booting"
LICENSE = "MIT"
PR = "r9"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

ARAGO_ALSA_BASE = "\
    libasound \
    alsa-utils-aplay \
    "

ARAGO_BASE = "\
    module-init-tools \
    mtd-utils \
    mtd-utils-ubifs \
    curl \
    initscript-telnetd \
    ethtool \
    thermal-init \
    bash \
    udev-extraconf \
    "

# these require meta-openembedded/meta-oe layer
ARAGO_EXTRA = "\
    devmem2 \
    tcpdump \
    "

# minimal set of packages - needed to boot
RDEPENDS_${PN} = "\
    ${@bb.utils.contains('DISTRO_FEATURES', 'zeroconf', 'packagegroup-base-zeroconf', '',d)} \
    ${@bb.utils.contains('MACHINE_FEATURES', 'alsa', '${ARAGO_ALSA_BASE}', '',d)} \
    ${ARAGO_BASE} \
    ${ARAGO_EXTRA} \
    "

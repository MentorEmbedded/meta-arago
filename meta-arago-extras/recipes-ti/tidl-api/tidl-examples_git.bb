SUMMARY = "TIDL API examples"
DESCRIPTION = "Examples of TIDL API. Refer TIDL API User's Guide for details."
HOMEPAGE = "https://downloads.ti.com/mctools/esd/docs/tidl-api/intro.html"
LICENSE = "BSD"

include tidl-api.inc
require recipes-ti/includes/ti-paths.inc

PR = "${INC_PR}.1"
SRC_URI += "file://0002-replace-2-dsp-2-group-layer-use-cases-with-1-dsp.patch"

COMPATIBLE_MACHINE = "dra7xx"
PACKAGE_ARCH = "${MACHINE_ARCH}"

DEPENDS = "opencv \
           clocl-native \
           ti-cgt6x-native \
           tidl-api \
           json-c \
"

RDEPENDS_${PN} += "tidl-api \
                   opencl-runtime \
                   opencv \
                   json-c \
"

EXTRA_OEMAKE = " -C ${S}/examples \
                 TARGET_ROOTDIR=${STAGING_DIR_HOST} \
                 TI_OCL_CGT_INSTALL=${STAGING_DIR_NATIVE}/usr/share/ti/cgt-c6x \
"

do_compile() {
  oe_runmake
}

CP_ARGS="-Prf --preserve=mode,timestamps --no-preserve=ownership"

TIDL_INSTALL_DIR = "${datadir}/ti/tidl"

do_install() {
    install -d ${D}${TIDL_INSTALL_DIR}
    install -d ${D}${TIDL_INSTALL_DIR}/examples
    cp ${CP_ARGS} ${S}/examples/* ${D}${TIDL_INSTALL_DIR}/examples/
}

FILES_${PN} += "\
    ${TIDL_INSTALL_DIR} \
"


INSANE_SKIP_${PN} = "arch ldflags textrel staticdev"

DESCRIPTION = "TI OpenCL host runtime and development packages"
HOMEPAGE = "https://downloads.ti.com/mctools/esd/docs/opencl/index.html"
LICENSE = "BSD"

include ocl.inc
SRC_URI += "file://0001-OpenCL_K2x_compile.patch;patchdir=${WORKDIR}/git"

PR = "${INC_PR}.1"

inherit cmake systemd

COMPATIBLE_MACHINE = "dra7xx|keystone"
PACKAGE_ARCH = "${MACHINE_ARCH}"

PACKAGES =+ "${PN}-runtime"

DEPENDS = " ocl-gl-headers \
            opencl-monitor \
            cmake-native \
            cmem \
            ti-llvm3.6 \
            ti-llvm3.6-native \
            binutils \
            sqlite3 \
            libffi \
            boost \
            boost-native \
            libloki \
            pkgconfig-native \
            libulm \
            elfutils \
            json-c \
"

DEPENDS_append_dra7xx   = " ti-ipc virtual/kernel"
DEPENDS_append_keystone = " mpm-transport multiprocmgr"
DEPENDS_remove_k2g = " libulm"

MONITORS                      = " opencl-monitor"
MONITORS_append_am57xx-evm    = " opencl-monitor-ipu"
MONITORS_append_am57xx-hs-evm = " opencl-monitor-ipu"
MONITORS_append_dra7xx        = " opencl-monitor-ipu"

RDEPENDS_${PN} += " bash"
RDEPENDS_${PN}-dev += " ocl-gl-headers-dev opencl-monitor"
RDEPENDS_${PN}-runtime += " ${PN} ${MONITORS} clocl ti-cgt6x"

ALLOW_EMPTY_${PN}-runtime = "1"

S = "${WORKDIR}/git/host"

export WANT_LLVM_RELEASE = "3.6-ti"

OCL_BUILD_TARGET_dra7xx = "ARM_AM57"
OCL_BUILD_TARGET_k2hk = "ARM_K2H"
OCL_BUILD_TARGET_k2l = "ARM_K2L"
OCL_BUILD_TARGET_k2e = "ARM_K2E"
OCL_BUILD_TARGET_k2g = "ARM_K2G"

ENABLE_ULM = "1"
ENABLE_ULM_k2g = "0"

EXTRA_OECMAKE += " -DBUILD_TARGET=${OCL_BUILD_TARGET} -DBUILD_OUTPUT=lib -DENABLE_ULM=${ENABLE_ULM} -DOCL_VERSION=${PV}"

EXTRA_OEMAKE += "KERNEL_INSTALL_DIR=${STAGING_KERNEL_DIR} LINUX_DEVKIT_ROOT=${STAGING_DIR_HOST}"
export KERNEL_INSTALL_DIR = "${STAGING_KERNEL_DIR}"

MCTD = "${S}/mct-daemon/ti-mct-daemon.service.k2x"
MCTD_dra7xx = "${S}/mct-daemon/ti-mct-daemon.service.am57x"

do_install_append() {
    install -d ${D}${systemd_system_unitdir}
    install -m0644 ${MCTD} ${D}${systemd_system_unitdir}/ti-mct-daemon.service
}

SYSTEMD_SERVICE_${PN} = "ti-mct-daemon.service"
SYSTEMD_AUTO_ENABLE_${PN} = "${@oe.utils.conditional("RESERVE_CMEM", "1", "enable", "disable", d)}"

FILES_${PN}-runtime += "${bindir}"

FILES_${PN} += " \
    ${datadir}/ti/opencl/* \
"

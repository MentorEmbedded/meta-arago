SUMMARY = "TI OpenCL compute device firmware for C66x (RTOS)"
HOMEPAGE = "http://software-dl.ti.com/mctools/docs/opencl/intro.html"
LICENSE = "BSD"

require recipes-ti/ocl/ocl.inc
require recipes-ti/includes/arago-paths.inc

PR = "${INC_PR}.0"

DEPENDS = " ti-llvm3.6-native \
            common-csl-ip-rtos \
            ti-xdctools-native \
            ti-ipc-rtos \
            ti-sysbios \
            ti-cgt6x-native \
            edma3-lld-rtos \
            ti-xdais \
            ti-framework-components \
            libaet \
            openmp-rtos \
            gcc-arm-none-eabi-native \
"

COMPATIBLE_MACHINE = "omap-a15"
PACKAGE_ARCH = "${MACHINE_ARCH}"

S = "${WORKDIR}/git/monitor"

BUILD_TARGET_omap-a15   = "ARM_AM57"

RELEASE_TARGET = ""
RELEASE_TARGET_omap-a15 = "am57xx"

export TI_OCL_CGT_INSTALL = "${STAGING_DIR_NATIVE}/usr/share/ti/cgt-c6x"
export PDK_DIR = "${PDK_INSTALL_DIR}"
export IPC_DIR = "${IPC_INSTALL_DIR}"
export XDC_DIR = "${XDC_INSTALL_DIR}"
export BIOS_DIR = "${SYSBIOS_INSTALL_DIR}"
export EDMA3LLD_DIR = "${EDMA3_LLD_INSTALL_DIR}"
export XDAIS_DIR = "${XDAIS_INSTALL_DIR}"
export FC_DIR = "${FC_INSTALL_DIR}"
export MPM_DIR = "${MPM_INSTALL_DIR}"
export OMP_DIR = "${OMP_INSTALL_DIR}"
export ULM_DIR ="${STAGING_DIR_TARGET}/usr/share/ti/ulm"
export GDB_SERVER_DIR = "${STAGING_DIR_TARGET}/usr/share/ti/gdbc6x"
export AET_DIR = "${STAGING_DIR_TARGET}/usr/share/ti/ctoolslib/aet"
export X86_LLVM_DIR = "${STAGING_DIR_NATIVE}/usr"
export XDCPATH = "${S};${IPC_DIR}/packages;${BIOS_DIR}/packages;${EDMA3LLD_DIR}/packages;${FC_DIR}/packages;${XDAIS_DIR}/packages"
export DESTDIR="${D}${OCL_RTOS_INSTALL_DIR_RECIPE}/ti-opencl-rtos-${RELEASE_TARGET}-${PV}/packages/ti/opencl"
export OCL_FPERMS = "664"
export OCL_DPERMS = "775"
export SHARE_PATH="${DESTDIR}${datadir}/ti/opencl"

EXTRA_OEMAKE += " BUILD_OS=SYS_BIOS \
                  WORKING_DIRECTORY=${S} \
                  BUILD_TARGET=${BUILD_TARGET} \
"

do_compile() {
  oe_runmake -f Makefile
}

do_install() {
    install -m ${OCL_DPERMS} -d ${SHARE_PATH}
    install -m ${OCL_FPERMS} monitor_am57x_rtos/dsp0.syms ${SHARE_PATH}/dsp.syms
    install -m ${OCL_FPERMS} monitor_am57x_rtos/dsp0.syms.obj ${SHARE_PATH}/dsp_syms.obj
    install -m ${OCL_FPERMS} monitor_am57x_rtos/dsp0.out ${SHARE_PATH}
    install -m ${OCL_FPERMS} monitor_am57x_rtos/dsp1.out ${SHARE_PATH}
    install -m ${OCL_FPERMS} libDSPMonitor.ae66 ${SHARE_PATH}
    install -m ${OCL_FPERMS} ../builtins/dsp.lib ${SHARE_PATH}
    install -m ${OCL_FPERMS} ../libm/libm.lib ${SHARE_PATH}
    install -m ${OCL_FPERMS} cmds/monitor.am57x_rtos.cmd ${SHARE_PATH}
}

FILES_${PN} += "${OCL_RTOS_INSTALL_DIR_RECIPE}"
INSANE_SKIP_${PN} = "arch"

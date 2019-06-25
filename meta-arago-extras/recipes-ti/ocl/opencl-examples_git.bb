SUMMARY = "TI OpenCL example applications"
HOMEPAGE = "https://downloads.ti.com/mctools/esd/docs/opencl/index.html"
LICENSE = "BSD"

include ocl.inc
require recipes-ti/includes/ti-paths.inc

SRC_URI += "file://0003-Fix-g-8.3.0-OpenCL-example-undefined-behavior.patch;patchdir=.."

PR = "${INC_PR}.1"

COMPATIBLE_MACHINE = "dra7xx|keystone"
PACKAGE_ARCH = "${MACHINE_ARCH}"

DEPENDS = "opencl opencl-monitor ti-cgt6x-native clocl-native"

OCL_PERSISTENT_DEPENDS = "ti-xdctools-native ti-ipc-rtos ti-sysbios"

DEPENDS_append_dra7xx = " ${OCL_PERSISTENT_DEPENDS}"

RDEPENDS_${PN} += " opencl-runtime"
RDEPENDS_${PN}-dev += " libgomp-dev"

S = "${WORKDIR}/git/opencl_example_src"
B = "${S}"

OCL_EXAMPLE_LIST = " abort_exit \
                     buffer \
                     ccode \
                     conv1d \
                     dgemm \
                     dspheap \
                     dsplib_fft \
                     edmamgr \
                     float_compute \
                     matmpy \
                     monte_carlo \
                     null \
                     offline \
                     offline_embed \
                     ooo_callback \
                     platforms \
                     sgemm \
                     simple \
                     timeout \
                     vecadd \
                     vecadd_openmp \
                     vecadd_openmp_t \
                     vecadd_subdevice \
"

OCL_PERSISTENT_EXAMPLE_LIST = " persistent_clock_concurrent \
                                persistent_clock_spanning \
                                persistent_common \
                                persistent_kernel_timeout \
                                persistent_messageq_concurrent \
                                persistent_task_concurrent \
                                persistent_task_spanning \
"

OCL_EXAMPLE_LIST_append_dra7xx = " ${OCL_PERSISTENT_EXAMPLE_LIST}"

OCL_MPAX_EXAMPLE_LIST = " vecadd_mpax \
                          vecadd_mpax_openmp \
"

OCL_EXAMPLE_LIST_append_k2hk = " ${OCL_MPAX_EXAMPLE_LIST}"

python do_unpack_append() {
    s = d.getVar("S")
    os.makedirs(s)
}

python do_patch_append() {
    import shutil
    git_dir = d.expand("${WORKDIR}/git/examples")
    s = d.getVar("S")
    shutil.copy(os.path.join(git_dir,"Makefile"),s)
    shutil.copy(os.path.join(git_dir,"make.inc"),s)
    for example in d.getVar("OCL_EXAMPLE_LIST").split():
        shutil.copytree(os.path.join(git_dir,example), os.path.join(s,example))
}


EXTRA_OEMAKE = " TARGET_ROOTDIR=${STAGING_DIR_HOST} \
                 TI_OCL_CGT_INSTALL=${STAGING_DIR_NATIVE}/usr/share/ti/cgt-c6x \
"

export XDC_DIR = "${XDC_INSTALL_DIR}/packages"
export IPC_DIR = "${IPC_INSTALL_DIR}/packages"
export BIOS_DIR = "${SYSBIOS_INSTALL_DIR}/packages"

do_install() {
    install -d ${D}${datadir}/ti/examples/opencl

    install ${B}/Makefile ${D}${datadir}/ti/examples/opencl
    install ${B}/make.inc ${D}${datadir}/ti/examples/opencl

    for ocl_example in ${OCL_EXAMPLE_LIST}; do
        install -d ${D}${datadir}/ti/examples/opencl/${ocl_example}
        cp -rv ${B}/${ocl_example}/* ${D}${datadir}/ti/examples/opencl/${ocl_example}
    done
}

FILES_${PN} += "\
    ${datadir}/ti/examples/opencl \
"

FILES_${PN}-dbg += "\
    ${datadir}/ti/examples/opencl/*/.debug \
"

INSANE_SKIP_${PN} = "arch ldflags textrel staticdev"

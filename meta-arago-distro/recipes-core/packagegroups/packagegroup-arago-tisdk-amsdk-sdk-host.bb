DESCRIPTION = "Task to install additional scripts and applications into the SDK"
LICENSE = "MIT"
PR = "r22"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

# Choose the kernel and u-boot recipe sources to use

U-BOOT_SRC = "${PREFERRED_PROVIDER_virtual/bootloader}-src"
KERNEL_SRC = "${PREFERRED_PROVIDER_virtual/kernel}-src"

KERNEL_SHADOW_CLONE := "${PREFERRED_PROVIDER_virtual/kernel}"
KERNEL_SHADOW_CLONE_ENABLED := "${SRCIPK_SHALLOW_CLONE_pn-${KERNEL_SHADOW_CLONE}}"

TOOLS = ""
TOOLS_omap-a15 = ""
TOOLS_ti43x = ""
TOOLS_keystone = ""
TOOLS_append_am37x-evm = " flash-utility"
TOOLS_append_am3517-evm = " flash-utility"

EXTRA_FILES = ""
EXTRA_FILES_ti43x = "tisdk-uenv"
EXTRA_FILES_omap-a15 = "tisdk-readme tisdk-uenv"

RDEPENDS_${PN} = "\
    ${TOOLS} \
    ti-tisdk-setup \
    ${EXTRA_FILES} \
    ti-tisdk-makefile \
    ${U-BOOT_SRC} \
    ${KERNEL_SRC} \
    ${@base_conditional('KERNEL_SHADOW_CLONE_ENABLED','true','unshallow-repositories','',d)} \
    tisdk-install \
"

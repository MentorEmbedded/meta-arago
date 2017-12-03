PV = "3.6.0"
INC_PR = "r0"

LIC_FILES_CHKSUM = "file://LICENSE.TXT;md5=47e311aa9caedd1b3abf098bd7814d1d"

LLVM_GIT_NAME = "llvm"
LLVM_GIT_URI = "git://git.ti.com/opencl/llvm.git"
LLVM_GIT_PROTOCOL = "git"
LLVM_GIT_BRANCH = "release_36_ti"
LLVM_GIT_DESTSUFFIX = "git"
LLVM_GIT_SRCREV = "8ff2a35c586a3d3f624712e0943777613a017bc5"

SRC_URI = "${LLVM_GIT_URI};protocol=${LLVM_GIT_PROTOCOL};branch=${LLVM_GIT_BRANCH};destsuffix=${LLVM_GIT_DESTSUFFIX};name=${LLVM_GIT_NAME}"

SRCREV_${LLVM_GIT_NAME} = "${LLVM_GIT_SRCREV}"
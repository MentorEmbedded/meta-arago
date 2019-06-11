TOOLCHAIN_HOST_TASK ?= "nativesdk-packagegroup-arago-sdk-host"
TOOLCHAIN_HOST_TASK += "nativesdk-buildtools-perl-dummy"
TOOLCHAIN_HOST_TASK += "packagegroup-arago-cross-canadian-${MACHINE}"
TOOLCHAIN_TARGET_TASK ?= "packagegroup-arago-standalone-sdk-target"
TOOLCHAIN_SUFFIX ?= "-sdk"
TOOLCHAIN_OUTPUTNAME ?= "${SDK_NAME}-${ARMPKGARCH}-${TARGET_OS}${TOOLCHAIN_SUFFIX}"
TOOLCHAIN_CLEANUP_PACKAGES ?= ""
SDKIMAGE_FEATURES = "package-management"
SDK_PACKAGE_ARCHS += "buildtools-dummy-${SDKPKGSUFFIX}"

inherit toolchain-scripts

require recipes-core/meta/meta-toolchain.bb

PR = "r35"

XZ_COMPRESSION_LEVEL ?= "-e -6"
XZ_INTEGRITY_CHECK ?= "crc32"
XZ_THREADS ?= "-T 0"

# This function creates an environment-setup-script for use in a deployable SDK
toolchain_create_sdk_env_script () {
	# Create environment setup script
	script=${SDK_OUTPUT}/${SDKPATH}/environment-setup
	rm -f $script
	touch $script
	echo 'SDK_PATH="${SDKPATH}"' >> $script
	echo 'if [ -z "$ZSH_NAME" ] && [ "x$0" = "x./environment-setup" ]; then' >> $script
	echo '    echo "Error: This script needs to be sourced. Please run as \". ./environment-setup\""' >> $script
	echo '    exit 1' >> $script
	echo 'else' >> $script
	echo '    if [ -n "$BASH_SOURCE" ]; then' >> $script
	echo '        SDK_PATH="`dirname $BASH_SOURCE`"' >> $script
	echo '    fi' >> $script
	echo '    SDK_PATH=`readlink -f "$SDK_PATH"`' >> $script
	echo '    export SDK_PATH' >> $script
	echo 'fi' >> $script
	echo 'export SDK_SYS=${SDK_SYS}' >> $script
	echo 'export REAL_MULTIMACH_TARGET_SYS=${REAL_MULTIMACH_TARGET_SYS}' >> $script
	echo 'export TOOLCHAIN_SYS=${TOOLCHAIN_SYS}' >> $script
	echo 'export TOOLCHAIN_PREFIX=$TOOLCHAIN_SYS-' >> $script
	echo 'export SDK_PATH_NATIVE=$SDK_PATH/sysroots/$SDK_SYS' >> $script
	echo 'export SDK_PATH_TARGET=$SDK_PATH/sysroots/$REAL_MULTIMACH_TARGET_SYS' >> $script
	echo 'export PATH=$SDK_PATH_NATIVE${bindir_nativesdk}:$SDK_PATH_NATIVE${bindir_nativesdk}/${TARGET_ARCH}${TARGET_VENDOR}-${TARGET_OS}:$PATH' >> $script
	echo 'export CPATH=$SDK_PATH_TARGET/usr/include:$CPATH' >> $script
	echo 'export PKG_CONFIG_SYSROOT_DIR=$SDK_PATH_TARGET' >> $script
	echo 'export PKG_CONFIG_PATH=$SDK_PATH_TARGET${libdir}/pkgconfig' >> $script
	echo 'export PKG_CONFIG_ALLOW_SYSTEM_LIBS=1' >> $script
	echo 'export CONFIG_SITE=$SDK_PATH/site-config-$REAL_MULTIMACH_TARGET_SYS' >> $script
	printf 'export CC="\x24{TOOLCHAIN_PREFIX}gcc --sysroot=$SDK_PATH_TARGET"\n' >> $script
	printf 'export CXX="\x24{TOOLCHAIN_PREFIX}g++ --sysroot=$SDK_PATH_TARGET"\n' >> $script
	printf 'export GDB=\x24{TOOLCHAIN_PREFIX}gdb\n' >> $script
	printf 'export CPP="\x24{TOOLCHAIN_PREFIX}gcc -E --sysroot=$SDK_PATH_TARGET"\n' >> $script
	printf 'export LD="\x24{TOOLCHAIN_PREFIX}ld --sysroot=$SDK_PATH_TARGET"\n' >> $script
	printf 'export NM=\x24{TOOLCHAIN_PREFIX}nm\n' >> $script
	printf 'export AS=\x24{TOOLCHAIN_PREFIX}as\n' >> $script
	printf 'export AR=\x24{TOOLCHAIN_PREFIX}ar\n' >> $script
	printf 'export RANLIB=\x24{TOOLCHAIN_PREFIX}ranlib\n' >> $script
	printf 'export OBJCOPY=\x24{TOOLCHAIN_PREFIX}objcopy\n' >> $script
	printf 'export OBJDUMP=\x24{TOOLCHAIN_PREFIX}objdump\n' >> $script
	printf 'export STRIP=\x24{TOOLCHAIN_PREFIX}strip\n' >> $script
	echo 'export CONFIGURE_FLAGS="--target=${TARGET_SYS} --host=${TARGET_SYS} --build=${SDK_ARCH}-linux --with-libtool-sysroot=$SDK_PATH_TARGET"' >> $script
	echo 'export CPPFLAGS="${TARGET_CC_ARCH} --sysroot=$SDK_PATH_TARGET"' >> $script
	echo 'export CFLAGS="$CPPFLAGS"' >> $script
	echo 'export CXXFLAGS="$CPPFLAGS"' >> $script
	echo 'export LDFLAGS="${TARGET_LD_ARCH} --sysroot=$SDK_PATH_TARGET"' >> $script
	echo 'export OECORE_NATIVE_SYSROOT=$SDK_PATH_NATIVE' >> $script
	echo 'export OECORE_TARGET_SYSROOT=$SDK_PATH_TARGET' >> $script
	echo 'export OECORE_ACLOCAL_OPTS="-I $SDK_PATH_NATIVE/usr/share/aclocal"' >> $script
	echo 'export OECORE_DISTRO_VERSION="${DISTRO_VERSION}"' >> $script
	echo 'export OECORE_SDK_VERSION="${SDK_VERSION}"' >> $script
}

SDK_POSTPROCESS_COMMAND_prepend = "arago_sdk_fixup; "

arago_sdk_fixup () {
	# Remove broken .la files
	for i in `find ${SDK_OUTPUT}/${SDKPATH} -name \*.la`; do
		rm -f $i
	done

	cleanup_toolchain_packages

	# Remove python binaries
	# gdb links with libpython and loads core modules, which bundled with binaries
	rm -rf ${SDK_OUTPUT}/${SDKPATHNATIVE}${bindir_nativesdk}/python*

	# Do some extra setup work due to new structure
	mkdir -p "${SDK_OUTPUT}/${SDKPATHNATIVE}${prefix_nativesdk}/lib/${TARGET_ARCH}${TARGET_VENDOR}-${TARGET_OS}"
	if [ "${TOOLCHAIN_TYPE}" = "internal" ]; then
		lexec="${SDK_OUTPUT}/${SDKPATHNATIVE}${prefix_nativesdk}/libexec/${TARGET_ARCH}${TARGET_VENDOR}-${TARGET_OS}/gcc/${TOOLCHAIN_SYS}"
	else
		lexec="${SDK_OUTPUT}/${SDKPATHNATIVE}${prefix_nativesdk}/libexec/gcc/${TOOLCHAIN_SYS}"
	fi
	tcv=`ls -1 $lexec|head -1`
	[ -e ${SDK_OUTPUT}/${SDKTARGETSYSROOT}/lib/$tcv ] || ln -s . ${SDK_OUTPUT}/${SDKTARGETSYSROOT}/lib/$tcv
	[ -e ${SDK_OUTPUT}/${SDKTARGETSYSROOT}/usr/lib/$tcv ] || ln -s . ${SDK_OUTPUT}/${SDKTARGETSYSROOT}/usr/lib/$tcv
	[ -e ${SDK_OUTPUT}/${SDKTARGETSYSROOT}/lib/${TOOLCHAIN_SYS} ] || ln -s . ${SDK_OUTPUT}/${SDKTARGETSYSROOT}/lib/${TOOLCHAIN_SYS}
	[ -e ${SDK_OUTPUT}/${SDKTARGETSYSROOT}/usr/lib/${TOOLCHAIN_SYS} ] || ln -s . ${SDK_OUTPUT}/${SDKTARGETSYSROOT}/usr/lib/${TOOLCHAIN_SYS}
	tcpath="${SDK_OUTPUT}/${SDKPATHNATIVE}${prefix_nativesdk}/${TOOLCHAIN_SYS}"
	mkdir -p $tcpath
	pushd $tcpath
	ln -s ${SDKTARGETSYSROOT}/include include
	if [ "${TOOLCHAIN_BRAND}" != "arago" ]; then
		mkdir -p libc
		cd libc
	fi
	mkdir -p usr
	ln -s ${SDKTARGETSYSROOT}/lib lib
	ln -s ${SDKTARGETSYSROOT}/usr/lib usr/lib
	ln -s ${SDKTARGETSYSROOT}/usr/include usr/include
	popd
}

fakeroot create_sdk_files() {
	# Setup site file for external use
	toolchain_create_sdk_siteconfig ${SDK_OUTPUT}/${SDKPATH}/site-config-${REAL_MULTIMACH_TARGET_SYS}

	toolchain_create_sdk_env_script ${SDK_OUTPUT}/${SDKPATH}/environment-setup

	# Add version information
	toolchain_create_sdk_version ${SDK_OUTPUT}/${SDKPATH}/version-${REAL_MULTIMACH_TARGET_SYS}

	cp ${COREBASE}/scripts/relocate_sdk.py ${SDK_OUTPUT}/${SDKPATH}/

	# Replace the ##DEFAULT_INSTALL_DIR## with the correct pattern.
	# Escape special characters like '+' and '.' in the SDKPATH
	escaped_sdkpath=$(echo ${SDKPATH} |sed -e "s:[\+\.]:\\\\\\\\\0:g")
	sed -i -e "s:##DEFAULT_INSTALL_DIR##:$escaped_sdkpath:" ${SDK_OUTPUT}/${SDKPATH}/relocate_sdk.py
}

# Remove undesired packages that may be pulled into the toolchain by -dev
# package dependencies.  This is usually GPLv3 components.
cleanup_toolchain_packages() {
	if [ "${TOOLCHAIN_CLEANUP_PACKAGES}" != "" ]
	then
		# Clean up the native side of the toolchain
		opkg_dir="${SDK_OUTPUT}/${SDKPATHNATIVE}"
		opkg_conf="${SDK_OUTPUT}/etc/opkg-sdk.conf"
		opkg -o $opkg_dir -f $opkg_conf --force-depends remove ${TOOLCHAIN_CLEANUP_PACKAGES}

		# Clean up the target side of the toolchain
		opkg_dir="${SDK_OUTPUT}/${SDKTARGETSYSROOT}"
		opkg_conf="${opkg_dir}/etc/opkg.conf"
		opkg -o $opkg_dir -f $opkg_conf --force-depends remove ${TOOLCHAIN_CLEANUP_PACKAGES}

		# Clean up empty directories from cleaned up packages
		find ${SDK_OUTPUT} -depth -type d -empty -print0 | xargs -r0 /bin/rmdir
	fi
}

fakeroot tar_sdk() {
	# Package it up
	mkdir -p ${SDK_DEPLOY}
	cd ${SDK_OUTPUT}/${SDKPATH}
	tar --owner=root --group=root -c . | xz -f -k -c ${XZ_COMPRESSION_LEVEL} ${XZ_THREADS} --check=${XZ_INTEGRITY_CHECK} - > ${SDK_DEPLOY}/${TOOLCHAIN_OUTPUTNAME}.tar.xz
}

fakeroot create_shar() {
	cat << "EOF" > ${SDK_DEPLOY}/${TOOLCHAIN_OUTPUTNAME}.sh
#!/bin/bash

INST_ARCH=$(uname -m | sed -e "s/i[3-6]86/ix86/" -e "s/x86[-_]64/x86_64/")
SDK_ARCH=$(echo ${SDK_ARCH} | sed -e "s/i[3-6]86/ix86/" -e "s/x86[-_]64/x86_64/")

if [ "$INST_ARCH" != "$SDK_ARCH" ]; then
	# Allow for installation of ix86 SDK on x86_64 host
	if [ "$INST_ARCH" != x86_64 -o "$SDK_ARCH" != ix86 ]; then
		echo "Error: Installation machine not supported!"
		exit 1
	fi
fi

DEFAULT_INSTALL_DIR="${SDKPATH_REAL}"
TMPSDKPATH="${SDKPATH}"
SUDO_EXEC=""
target_sdk_dir=""
answer=""
relocate=1
savescripts=0
verbose=0
while getopts ":yd:DRS" OPT; do
	case $OPT in
	y)
		answer="Y"
		[ "$target_sdk_dir" = "" ] && target_sdk_dir=$DEFAULT_INSTALL_DIR
		;;
	d)
		target_sdk_dir=$OPTARG
		;;
	D)
		verbose=1
		;;
	R)
		relocate=0
		savescripts=1
		;;
	S)
		savescripts=1
		;;
	*)
		echo "Usage: $(basename $0) [-y] [-d <dir>]"
		echo "  -y         Automatic yes to all prompts"
		echo "  -d <dir>   Install the SDK to <dir>"
		echo "======== Advanced DEBUGGING ONLY OPTIONS ========"
		echo "  -S         Save relocation scripts"
		echo "  -R         Do not relocate executables"
		echo "  -D         use set -x to see what is going on"
		exit 1
		;;
	esac
done

if [ $verbose = 1 ] ; then
	set -x
fi

printf "Enter target directory for SDK (default: $DEFAULT_INSTALL_DIR): "
if [ "$target_sdk_dir" = "" ]; then
	read target_sdk_dir
	[ "$target_sdk_dir" = "" ] && target_sdk_dir=$DEFAULT_INSTALL_DIR
else
	echo "$target_sdk_dir"
fi

eval target_sdk_dir=$(echo "$target_sdk_dir"|sed 's/ /\\ /g')
if [ -d "$target_sdk_dir" ]; then
	target_sdk_dir=$(cd "$target_sdk_dir"; pwd)
else
	target_sdk_dir=$(readlink -m "$target_sdk_dir")
fi

if [ -n "$(echo $target_sdk_dir|grep ' ')" ]; then
	echo "The target directory path ($target_sdk_dir) contains spaces. Abort!"
	exit 1
fi

if [ -e "$target_sdk_dir/environment-setup*" ]; then
	echo "The directory \"$target_sdk_dir\" already contains a SDK for this architecture."
	printf "If you continue, existing files will be overwritten! Proceed[y/N]?"

	default_answer="n"
else
	printf "You are about to install the SDK to \"$target_sdk_dir\". Proceed[Y/n]?"

	default_answer="y"
fi

if [ "$answer" = "" ]; then
	read answer
	[ "$answer" = "" ] && answer="$default_answer"
else
	echo $answer
fi

if [ "$answer" != "Y" -a "$answer" != "y" ]; then
	echo "Installation aborted!"
	exit 1
fi

# Try to create the directory (this will not succeed if user doesn't have rights)
mkdir -p $target_sdk_dir >/dev/null 2>&1

# if don't have the right to access dir, gain by sudo 
if [ ! -x $target_sdk_dir -o ! -w $target_sdk_dir -o ! -r $target_sdk_dir ]; then 
	SUDO_EXEC=$(which "sudo")
	if [ -z $SUDO_EXEC ]; then
		echo "No command 'sudo' found, please install sudo first. Abort!"
		exit 1
	fi

	# test sudo could gain root right
	$SUDO_EXEC pwd >/dev/null 2>&1
	[ $? -ne 0 ] && echo "Sorry, you are not allowed to execute as root." && exit 1

	# now that we have sudo rights, create the directory
	$SUDO_EXEC mkdir -p $target_sdk_dir >/dev/null 2>&1
fi

payload_offset=$(($(grep -na -m1 "^MARKER:$" $0|cut -d':' -f1) + 1))

printf "Extracting SDK..."
tail -n +$payload_offset $0| $SUDO_EXEC tar xJ -C $target_sdk_dir
echo "done"

printf "Setting it up..."
# fix environment paths
for env_setup_script in `ls $target_sdk_dir/environment-setup*`; do
	$SUDO_EXEC sed -e "s:$TMPSDKPATH:$target_sdk_dir:g" -i $env_setup_script
done

# fix dynamic loader paths in all ELF SDK binaries
native_sysroot=$target_sdk_dir/sysroots/${SDK_SYS}
dl_path=$($SUDO_EXEC find $native_sysroot/lib -name "ld-linux*")
if [ "$dl_path" = "" ] ; then
	echo "SDK could not be set up. Relocate script unable to find ld-linux.so. Abort!"
	exit 1
fi
executable_files=$($SUDO_EXEC find $native_sysroot -type f -perm /111 -exec file '{}' \;| grep "\(executable\|dynamically linked\)" | cut -f 1 -d ':')

tdir=`mktemp -d`
if [ x$tdir = x ] ; then
	echo "SDK relocate failed, could not create a temporary directory"
	exit 1
fi
echo "#!/bin/bash" > $tdir/relocate_sdk.sh
echo exec ${env_setup_script%/*}/relocate_sdk.py $target_sdk_dir $dl_path $executable_files >> $tdir/relocate_sdk.sh
$SUDO_EXEC mv $tdir/relocate_sdk.sh ${env_setup_script%/*}/relocate_sdk.sh
$SUDO_EXEC chmod 755 ${env_setup_script%/*}/relocate_sdk.sh
rm -rf $tdir
if [ $relocate = 1 ] ; then
	$SUDO_EXEC ${env_setup_script%/*}/relocate_sdk.sh
	if [ $? -ne 0 ]; then
		echo "SDK could not be set up. Relocate script failed. Abort!"
		exit 1
	fi
fi

# replace ${SDKPATH} with the new prefix in all text files: configs/scripts/etc
$SUDO_EXEC find $native_sysroot -type f -exec file '{}' \;|grep ":.*\(ASCII\|script\|source\).*text"|cut -d':' -f1|$SUDO_EXEC xargs sed -i -e "s:$TMPSDKPATH:$target_sdk_dir:g"

# change all symlinks pointing to ${SDKPATH}
for l in $($SUDO_EXEC find $native_sysroot -type l); do
	$SUDO_EXEC ln -sfn $(readlink $l|$SUDO_EXEC sed -e "s:$TMPSDKPATH:$target_sdk_dir:") $l
done

# find out all perl scripts in $native_sysroot and modify them replacing the
# host perl with SDK perl.
for perl_script in $($SUDO_EXEC find $native_sysroot -type f -exec grep "^#!.*perl" -l '{}' \;); do
	$SUDO_EXEC sed -i -e "s:^#! */usr/bin/perl.*:#! /usr/bin/env perl:g" -e \
		"s: /usr/bin/perl: /usr/bin/env perl:g" $perl_script
done

echo done

# delete the relocating script, so that user is forced to re-run the installer
# if he/she wants another location for the sdk
if [ $savescripts = 0 ] ; then
	$SUDO_EXEC rm ${env_setup_script%/*}/relocate_sdk.py ${env_setup_script%/*}/relocate_sdk.sh
fi

echo "SDK has been successfully set up and is ready to be used."

exit 0

MARKER:
EOF
	# append the SDK tarball
	cat ${SDK_DEPLOY}/${TOOLCHAIN_OUTPUTNAME}.tar.xz >> ${SDK_DEPLOY}/${TOOLCHAIN_OUTPUTNAME}.sh

	# delete the old tarball, we don't need it anymore
	rm ${SDK_DEPLOY}/${TOOLCHAIN_OUTPUTNAME}.tar.xz
}

DESCRIPTION = "Matrix GUI Application launcher"
HOMEPAGE = "https://gforge.ti.com/gf/project/matrix-gui-v2/"

LICENSE = "BSD & MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=a886c9ef769b2d8271115d2502512e5d"

SECTION = "multimedia"

PR = "r37"

SYSTEMD_SERVICE_${PN} = "matrix-gui-2.0.service"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit systemd

BRANCH ?= "master"
SRCREV = "9a2d12590102fefc5c29fc9e8d346ce6b0198468"

MATRIX_INITSCRIPT = "${@oe.utils.conditional('QT_PROVIDER', 'qt5', bb.utils.contains('DISTRO_FEATURES', 'wayland', 'init', 'init.eglfs', d), 'init', d)}"

SRC_URI = "git://git.ti.com/matrix-gui-v2/matrix-gui-v2.git;protocol=git;branch=${BRANCH} \
           file://${MATRIX_INITSCRIPT} \
           file://php.ini \
           file://matrix-gui-2.0.service \
           ${@oe.utils.conditional('QT_PROVIDER', 'qt5', bb.utils.contains('DISTRO_FEATURES', 'wayland', '', 'file://0001-execute_command-Stop-matrix-when-running-a-GUI-demo.patch', d), '', d)} \
           ${@bb.utils.contains('DISTRO_FEATURES', 'wayland', 'file://0001-execute_command-workaround-for-GUI-apps-with-weston.patch', '', d)} \
"

require matrix-gui-paths.inc

S = "${WORKDIR}/git"

MATRIX_FLAGS = "${@oe.utils.conditional('QT_PROVIDER','qt5','','-qws',d)}"
SWITCH_FOREGROUND_VT = "${@oe.utils.conditional('QT_PROVIDER','qt5','','chvt 4',d)}"
do_install(){
	install -d ${D}${MATRIX_BASE_DIR}
	install -d ${D}${MATRIX_WEB_DIR}
	cp -rfL ${S}/* ${D}${MATRIX_WEB_DIR}

	# Install our php.ini file
	install -m 0644 ${WORKDIR}/php.ini ${D}${MATRIX_BASE_DIR}/

	# Set the proper path in the init script
	sed -i -e s=__MATRIX_WEB_DIR__=${MATRIX_WEB_DIR}= ${WORKDIR}/${MATRIX_INITSCRIPT}
	sed -i -e "s/__MATRIX_FLAGS__/\"${MATRIX_FLAGS}\"/" ${WORKDIR}/${MATRIX_INITSCRIPT}
	sed -i -e "s/__SWITCH_FOREGROUND_VT__/${SWITCH_FOREGROUND_VT}/" ${WORKDIR}/${MATRIX_INITSCRIPT}

	# Install the script
	install -d ${D}${sysconfdir}/init.d
	install -m 0755 ${WORKDIR}/${MATRIX_INITSCRIPT} ${D}${sysconfdir}/init.d/matrix-gui-2.0

	# Install the systemd unit file
	install -d ${D}${systemd_system_unitdir}
	install -m 0644 ${WORKDIR}/matrix-gui-2.0.service ${D}${systemd_system_unitdir}
}

GUIDEPS = "matrix-gui-browser refresh-screen"
GUIDEPS_keystone = ""
GUIDEPS_omapl138 = ""
GUIDEPS_j7-evm = ""

RDEPENDS_${PN} += "matrix-lighttpd-config lighttpd lighttpd-module-cgi lighttpd-module-compress lighttpd-module-expire php php-cgi php-cli ${GUIDEPS}"

FILES_${PN} += "${MATRIX_BASE_DIR}/*"

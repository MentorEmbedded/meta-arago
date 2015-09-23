DESCRIPTION = "Task to add Qt embedded related packages"
LICENSE = "MIT"
PR = "r9"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

QT4_SGX_SUPPORT = "\
    qt4-embedded-plugin-gfxdriver-gfxpvregl \
    libqt-embeddedopengl4 \
    libqt-embeddedpvrqwswsegl4 \
"

QT4_ESSENTIALS = "\
    qt4-embedded \
    qt4-embedded-plugin-mousedriver-tslib \
    qt4-embedded-plugin-gfxdriver-gfxtransformed \
    qt4-embedded-plugin-phonon-backend-gstreamer \
    qt4-embedded-plugin-imageformat-gif \
    qt4-embedded-plugin-imageformat-jpeg \
    qt4-embedded-qml-plugins \
    libqt-embeddedmultimedia4 \
    libqt-embeddeddeclarative4 \
    libqt-embeddedxmlpatterns4 \
    ${@base_conditional('ARAGO_QT_PROVIDER', 'qt4-embedded-gles', '${QT4_SGX_SUPPORT}', '', d)} \
"

QT5_ESSENTIALS = "\
    qtbase-plugins \
    qtdeclarative-plugins \
    qtdeclarative-qmlplugins \
    qtlocation-plugins \
    qtlocation-qmlplugins \
    qtmultimedia-plugins \
    qtmultimedia-qmlplugins \
    ${@base_contains('DISTRO_FEATURES', 'wayland', 'qtwayland-plugins', '', d)} \
    qt3d-qmlplugins \
    qtwebkit-qmlplugins \
    qtquick1-plugins \
    qtquick1-qmlplugins \
"

RDEPENDS_${PN} = "\
    ${@base_conditional('QT_PROVIDER', 'qt5', "${QT5_ESSENTIALS}", "${QT4_ESSENTIALS}", d)} \
"

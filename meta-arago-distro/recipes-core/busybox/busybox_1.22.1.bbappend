# look for files in this layer first
FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

PR_append = "-arago15"

SRC_URI += " \
	file://inetd \
	file://inetd.conf \
"

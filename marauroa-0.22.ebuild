inherit java-pkg

S="${WORKDIR}/${PN}"
DESCRIPTION="Marauroa is a Massively Multiplayer Engine"
HOMEPAGE="http://marauroa.sourceforge.net"
SRC_URI="mirror://sourceforge/marauroa/${PN}-${PV}-src.tar.gz"
LICENSE="GPL"
KEYWORDS="x86 sparc ppc amd64"
DEPEND=">=dev-java/ant-1.4.1 junit"
RDEPEND=">=virtual/jdk-1.4 junit dev-java/jdbc-mysql"
RESTRICT="nomirror"

src_compile() {
	antopts="-Dversion=${PV}"
	export CLASSPATH="/usr/share/junit/lib/junit.jar"
	export DEP_APPEND="junit jdbc-mysql"
	ant ${antopts} compile test docs|| die "Compile failed"
}

src_install () {
	# install jswat classes
	mv lib/${PN}-${PV}.jar lib/${PN}.jar
	# java-pkg_dojar lib/${PN}.jar
    dojar lib/${PN}.jar
	# install documents
	dodoc COPYING AUTHORS BUGS HISTORY LICENSE TODO README docs
	
	dohtml -r javadocs
	cp javadocs/package-list ${D}/usr/share/doc/${PN}-${PV}/html/javadocs/package-list						
	dodir /etc/${PN}
    cp build/marauroa.ini  build/simplegame.ini build/the1001.ini ${D}/etc/${PN}/
	dodir /usr/bin/
	cp marauroad JMarauroa  ${D}/usr/bin/
	#install default
}

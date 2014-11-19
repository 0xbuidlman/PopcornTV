package com.softwarrior.libtorrent;

public class ProxyType {
	// 0 - none, // a plain tcp socket is used, and the other settings are
	// ignored.
	// 1 - socks4, // socks4 server, requires username.
	// 2 - socks5, // the hostname and port settings are used to connect to the
	// proxy. No username or password is sent.
	// 3 - socks5_pw, // the hostname and port are used to connect to the proxy.
	// the username and password are used to authenticate with the proxy server.
	// 4 - http, // the http proxy is only available for tracker and web seed
	// traffic assumes anonymous access to proxy
	// 5 - http_pw // http proxy with basic authentication uses username and
	// password

	public static final int NONE = 0;
	public static final int SOCKS_4 = 1;
	public static final int SOCKS_5 = 2;
	public static final int SOCKS_5_PW = 3;
	public static final int HTTP = 4;
	public static final int HTTP_PW = 5;
}
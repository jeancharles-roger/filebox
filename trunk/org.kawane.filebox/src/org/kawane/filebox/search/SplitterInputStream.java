package org.kawane.filebox.search;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SplitterInputStream extends InputStream {
	private static int defaultBufferSize = 8192;

	private InputStream in;
	// buffer variables
	private byte[] buf;
	private int bufLocalSize;
	private int consumedCursor = 0;

	// searched marker
	private byte[] marker;

	//  Boyer moore variables
	private int[] last;
	private int[] suffixes;
	private int[] match;
	int searchCursor = -1;
	int markerCursor = -1;

	// remember found
	private boolean found = false;
	private boolean consumed = false;

	public SplitterInputStream(InputStream in) {
		this(in, defaultBufferSize);
	}

	public SplitterInputStream(InputStream in, byte[] marker) {
		this.in = in;
		setMarker(marker);
	}

	public SplitterInputStream(InputStream in, byte[] marker, int bufSize) {
		this.in = in;
		buf = new byte[bufSize];
		setMarker(marker);
	}

	public SplitterInputStream(InputStream in, int bufSize) {
		this.in = in;
		buf = new byte[bufSize];
	}

	public void setMarker(byte[] marker) {
		if (this.marker != null) {
			searchCursor -= this.marker.length;
		}
		this.marker = marker;
		last = Search.computeLast(marker);
		suffixes = Search.computeSuffix(marker);
		match = Search.computeMatch(marker, suffixes);
		searchCursor += marker.length;
		markerCursor = marker.length - 1;
	}
	
	public byte[] getMarker() {
		return marker;
	}

	@Override
	public int read() throws IOException {
		byte[] b = new byte[1];
		read(b, 0, 1);
		return b[0];
	}

	public boolean hasNext() throws IOException {
		if(in.available() > 0) {
			return true;
		}
		return bufLocalSize > consumedCursor;
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (found && consumed) {
			// return an event that express that we have consume a marker
			found = false;
			consumed = false;
			markerCursor = marker.length - 1;
			return -1;
		}
		if (in.available() > 0) {
			int srcPos = searchCursor - (marker.length - 1);
			if (consumedCursor <= srcPos) {
				srcPos = consumedCursor;
			}
			int destPos = buf.length - srcPos;
			if (srcPos != 0) {
				System.arraycopy(buf, srcPos, buf, 0, destPos);
				consumedCursor = consumedCursor - srcPos;
				searchCursor = searchCursor - srcPos;
				bufLocalSize = in.read(buf, destPos, buf.length - destPos) + destPos;
			} else {
				bufLocalSize = in.read(buf, 0, buf.length);
			}
		} else {
			if (consumedCursor > bufLocalSize - 1) {
				return -1;
			}
			if(searchCursor >= bufLocalSize) {
				searchCursor = bufLocalSize + marker.length;
			}
		}
		if (found || search() != -1) {
			int readable = searchCursor - consumedCursor;
			found = true;
			if (readable > len) {
				System.arraycopy(buf, consumedCursor, b, off, len);
				consumedCursor += len;
				return len;
			} else {
				System.arraycopy(buf, consumedCursor, b, off, readable);
				// do not read the marker
				searchCursor = searchCursor + 2 * marker.length - 1;
				consumedCursor += readable + marker.length;
				// found and consumed
				consumed = true;
				return readable;
			}
		} else {
			int readable = searchCursor - marker.length - consumedCursor;
			if (readable > len) {
				System.arraycopy(buf, consumedCursor, b, off, len);
				consumedCursor += len;
				return len;
			} else if (readable <= 0) {
				return 0;
			} else {
				int read;
				if (readable < 0 || bufLocalSize - consumedCursor < readable) {
					read = bufLocalSize - consumedCursor;
				} else {
					read = readable;
				}
				if(b.length < read) {
					read = b.length;
				}
				System.arraycopy(buf, consumedCursor, b, off, read);
				consumedCursor += read;
				return read;
			}
		}

	}

	protected void debug() {
		System.err.println("\nStreamCursor: ");
		Search.debug(buf, bufLocalSize, searchCursor);
		System.err.println("\nReadCursor: ");
		Search.debug(buf, bufLocalSize, consumedCursor);
	}

	private int search() {
		while (searchCursor < bufLocalSize) {
			if (marker[markerCursor] == buf[searchCursor]) {
				if (markerCursor == 0) {
					return searchCursor;
				}
				markerCursor--;
				searchCursor--;
			} else {
				searchCursor += marker.length - markerCursor - 1
						+ Math.max(markerCursor - last[Search.getIndex(buf[searchCursor])], match[markerCursor]);
				markerCursor = marker.length - 1;
			}
		}
		return -1;
	}

	public static void main(String[] args) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		//		OutputStream out = System.err;
		String s = "-----------------------------154475214818464782371312986801  Content-Disposition: form-data; name=\"files\"; filename=\"coucou.txt\"  Content-Type: text/plain    coucou1 aaa bbb tttt t tt    -----------------------------154475214818464782371312986801  Content-Disposition: form-data; name=\"files\"; filename=\"coucou2.txt\"  Content-Type: text/plain    coucou2   -----------------------------154475214818464782371312986801--";
		System.out.println(s);
		InputStream stream = new ByteArrayInputStream(s.getBytes());
		
		SplitterInputStream in = new SplitterInputStream(stream);
		in.setMarker("-----------------------------154475214818464782371312986801".getBytes());
		byte[] b = new byte[1024];
		try {
			while(in.hasNext()) {
				int read = in.read(b);
				while (read != -1) {
					out.write(b, 0, read);
					read = in.read(b);
				}
				out.write(("\nfound: " + new String(in.getMarker()) + "\n").getBytes());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		String result = new String(out.toByteArray());
		//		System.out.println("hello tata titi tutu ".equals(result));
		System.out.println(result);
	}

}

/**
 * 
 */
package com.synectiks.commons.entities.oak;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.nio.file.attribute.FileTime;
import java.util.Date;

import javax.jcr.PropertyType;

import org.apache.commons.io.FilenameUtils;
import org.apache.jackrabbit.ocm.manager.atomictypeconverter.impl.BinaryTypeConverterImpl;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;

import com.synectiks.commons.utils.IUtils;

/**
 * @author Rajesh
 */
@Node(jcrType = "nt:unstructured")
public class OakFileNode extends OakEntity {

	private static final long serialVersionUID = 3473027035476995787L;
	public static enum FIELDS {
		contentType,
		createdAt,
		data,
		encoding,
		entityClass,
		jcrPath,
		name,
		path,
		updatedAt;
	};

	@Field(jcrType = PropertyType.TYPENAME_STRING)
	private String name;
	@Field(jcrType = PropertyType.TYPENAME_STRING)
	private String contentType;
	@Field(jcrType = PropertyType.TYPENAME_STRING)
	private String encoding;
	@Field(jcrType = PropertyType.TYPENAME_STRING)
	private String path;
	@Field(jcrType = PropertyType.TYPENAME_BINARY,
			converter = BinaryTypeConverterImpl.class)
	private InputStream data;

	public String getName() {
		return name;
	}

	public String getContentType() {
		return contentType;
	}

	public String getEncoding() {
		return encoding;
	}

	public String getPath() {
		return path;
	}

	public InputStream getData() {
		return data;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setData(InputStream data) {
		this.data = data;
	}

	public void setData(String data) {
		this.data = IUtils.convertStringToStream(data, this.encoding);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		if (!IUtils.isNullOrEmpty(name)) {
			builder.append("\"name\": \"" + name + "\"");
		}
		if (!IUtils.isNullOrEmpty(contentType)) {
			builder.append(builder.length() > 2 ? ", " : "");
			builder.append("\"contentType\": \"" + contentType + "\"");
		}
		if (!IUtils.isNullOrEmpty(encoding)) {
			builder.append(builder.length() > 2 ? ", " : "");
			builder.append("\"encoding\": \"" + encoding + "\"");
		}
		if (!IUtils.isNullOrEmpty(path)) {
			builder.append(builder.length() > 2 ? ", " : "");
			builder.append("\"path\": \"" + path + "\"");
		}
		if (!IUtils.isNullOrEmpty(getEntityClass())) {
			builder.append(builder.length() > 2 ? ", " : "");
			builder.append("\"entityClass\": \"" + getEntityClass() + "\"");
		}
		if (!IUtils.isNullOrEmpty(getJcrPath())) {
			builder.append(builder.length() > 2 ? ", " : "");
			builder.append("\"jcrPath\": \"" + getJcrPath() + "\"");
		}
		if (!IUtils.isNull(getCreatedAt())) {
			builder.append(builder.length() > 2 ? ", " : "");
			builder.append("\"createdAt\": \"" + getCreatedAt() + "\"");
		}
		if (!IUtils.isNull(getUpdatedAt())) {
			builder.append(builder.length() > 2 ? ", " : "");
			builder.append("\"updatedAt\": \"" + getUpdatedAt() + "\"");
		}
		/*if (!IUtils.isNull(datOa)) {
			builder.append(builder.length() > 2 ? ", " : "");
			try {
				builder.append("\"data\": \"" + IOUtils.toString(
						data, IConsts.DEF_ENCODING) + "\"");
			} catch (IOException e) {
				IUtils.logger.error("OakFileNode: " + e.getMessage(), e);
			}
		}*/
		builder.append("}");
		return builder.toString();
	}

	/**
	 * Method to create an oak file node from file
	 * @param jcrNodePath
	 * @param file
	 * @return
	 */
	public static OakFileNode createNode(String jcrNodePath, File file) {
		OakFileNode node = new OakFileNode();
		try {
			if (!IUtils.isNullOrEmpty(jcrNodePath)) {
				node.setJcrPath(jcrNodePath);
			}
			node.setContentType(URLConnection.guessContentTypeFromName(file.getName()));
			FileTime dt = IUtils.getFileAttribute(file, "createdAt");
			node.setCreatedAt(new Date(dt.toMillis()));
			dt = IUtils.getFileAttribute(file, "updatedAt");
			node.setUpdatedAt(new Date(dt.toMillis()));
			IUtils.logger.info("OakFileNode: before get file name");
			node.setName(FilenameUtils.getName(file.getName()));
			IUtils.logger.info("OakFileNode: after get file name");
			node.setPath(file.getAbsolutePath());
			FileInputStream fisTmp = new FileInputStream(file);
			node.setEncoding(new InputStreamReader(fisTmp).getEncoding());
			fisTmp.close();
			IUtils.logger.info("OakFileNode: stream encoding: " + node.getEncoding());
			return node;
		} catch (Throwable th) {
			th.printStackTrace();
		}
		return null;
	}

}

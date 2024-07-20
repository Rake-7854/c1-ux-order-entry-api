package com.rrd.c1ux.api.models.custdocs;

import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Schema(name ="InsertUploadFileApiRequest", description = "Request Class for cust docs UI page when the detail is needed for an already uploaded file within the UI.", type = "object")
public class InsertUploadFileApiRequest {
	@Schema(name = "fileName", description = "Just the file name", type = "String")
	private String fileName = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "fileId", description = "Just the file id", type = "String")
	private String fileId = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "deleteWhenDown", description = "Delete the file when done with it.", type = "String")
	private boolean deleteWhenDown = false;
}

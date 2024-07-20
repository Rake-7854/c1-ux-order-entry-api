package com.rrd.c1ux.api.models.admin;

import javax.validation.constraints.Size;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="PABImportResponse", description = "Response Class for import addresses in Personal Address Book", type = "object")

public class PABImportResponse extends BaseResponse{
	public enum errorcode {
	    MULTIPLE_SHEET, 
	    CONVERTION_ERROR,  
	}
	@Schema(name ="numWorksheets", description = "Number of Work sheets", type = "int", example = "2")
	@Size(min = 1)
	private int numWorksheets;
	@Schema(name ="numOfTotalRows", description = "Number of total rows", type = "int", example = "3")
	@Size(min = 1)
	private int numOfTotalRows;
	@Schema(name ="rowsImported", description = "Number of rows imported", type = "int", example = "4")
	@Size(min = 1)
	private int rowsImported;
	@Schema(name = "failed", description = "Failed", type = "boolean", allowableValues = {"false", "true"})
	private boolean failed;
	@Schema(name = "errorCode", description = "Error Code", type = "string", example = "0323")
	private String errorCode;
	@Schema(name = "successMsg", description = "Success Message", type = "string", example = "Test success message")
	private String successMsg;
	@Schema(name = "errorMsg", description = "Error Message", type = "string", example = "Test error message")
	private String errorMsg;

}

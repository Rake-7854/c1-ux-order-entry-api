package com.rrd.c1ux.api.models.orders.ordersearch;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="OrderFileEmailDetailResponse", description = "Response Class to view specfic Email/Order file from URL link retrieved "
		+ "against each email in API '/api/orders/getorderfilecontent'", type = "object")
//CAP-38738
public class OrderFileEmailDetailResponse extends BaseResponse{
	@Schema(name ="headerLabel", description = "Email Information Header label of Email", type = "String", example="Email Information")
	private String headerLabel;		// “Email Information Label” - translation text key "emailInfoLbl"
	
	@Schema(name ="fromLabel", description = "From email address label", type = "String",example="From Address")
	private String fromLabel; 		// “From Address Label” - translation text key "newFromAddrLbl"
	
	@Schema(name ="fromValue", description = "From email address if present or null", type = "String", example="fromaddress@email.com")
	private String fromValue; 		// “From email address values"
	
	@Schema(name ="replyLabel", description = "Reply To email address label", type = "String", example="Reply To Address")
	private String replyLabel; 		// “Reply To email Address Label” - translation text key "newReplyToAddrLbl"
	
	@Schema(name ="replyValue", description = "Reply To email address. if present, single mail or more else null", type = "String", example="[replyto1@email.com, replyto2@email.com]")
	private String replyValue; 		// “Reply To email address value”
	
	@Schema(name ="toLabel", description = "To email address label", type = "String", example="Email To")
	private String toLabel; 		// “Email To Address Label” - translation text key "emailToLbl"
	
	@Schema(name ="toValue", description = "To email address. if present, single mail", type = "String", example="to@email.com")
	private String toValue; 		// “Email To addresses value”
	
	@Schema(name ="subjectLabel", description = "Subject of email address label", type = "String", example="Email Subject")
	private String subjectLabel; 	// “Email Subject Label”, - translation text key "newEmailSubjLbl"
	
	@Schema(name ="subjectValue", description = "Subject of email address", type = "String", example="(Routing ID: 00000000) This order has been sent to you for your approval.")
	private String subjectValue; 	// “Email Subject value",
	
	@Schema(name ="emailHtml", description = "Email Body in html", type = "String")
	private String emailHtml; 		// “Email file HTML)”
	
	@Schema(name ="status", description = "Email retrival status of an API", type = "String", example="Success or Fail")
	private String status;
	
	@Schema(name ="errorMessage", description = "Custom error message, if fail to retrieve", type = "String", example="Request could not be completed at this time.")
	private String errorMessage;
}

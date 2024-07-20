/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		-------------------------------------------------------------------------------
 *	02/22/24	S Ramachandran		CAP-47198				Initial Version. Added  basic user profile request class for save SelfReg user
 *	02/27/24	M Sakthi			CAP-47375				Extended profile request fields 
 *	02/28/24	L De Leon			CAP-47376				Added corporate profile fields
 *	02/29/24	Satishkumar A		CAP-47448				Added API for validate password
 *	03/04/24	M Sakthi			CAP-47617				Added API for attributes
 *	03/01/24	S Ramachandran		CAP-47629				Added check warning flag
 *	03/05/24	Satishkumar A		CAP-47616				Added API for UDF fields validation
 *	03/12/24	L De Leon			CAP-47775				Added default address source code
 */

package com.rrd.c1ux.api.models.selfreg;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Size;

import com.rrd.c1ux.api.models.admin.C1UserDefinedField;
import com.rrd.c1ux.api.models.admin.C1UserSiteAttribute;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "SelfRegistrationSaveRequest", description = "Request Class for saving the User's Profile in self-Reg", type = "object")
public class SelfRegistrationSaveRequest {

	@Schema(name = "userID", description = "SR Profile User ID", type = "String", example = "Joseph")
	@Size(min = 0, max = 16)
	public String userID = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "password", description = "SR Profile User Password", type = "String", example = "")
	public String password = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "firstName", description = "SR Profile First Name", type = "String", example = "Joseph")
	@Size(min = 0, max = 25)
	public String firstName = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "lastName", description = "SR Profile Last Name", type = "String", example = "Biden")
	@Size(min = 0, max = 25)
	public String lastName = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "email", description = "SR Profile Email Address", type = "String", example = "JKRowling@warnerbros.com")
	@Size(min = 0, max = 128)
	public String email = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "phone", description = "SR Profile Phone Number", type = "String", example = "1-800-588-2300")
	@Size(min = 0, max = 24)
	public String phone = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "selectedPatternAfter", description = "SR Selected Pattern after User", type = "String", example = "Biden")
	@Size(min = 0, max = 16)
	private String selectedPatternAfter;

	// CAP-47375-Extended Profile
	@Schema(name = "epName2", description = "Extended Profile Name2", type = "String", example = "Joe Smith")
	@Size(min = 0, max = 50)
	public String epName2 = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "epAddr1", description = "Extended Profile Address 1", type = "String", example = "321 Home St")
	@Size(min = 0, max = 50)
	public String epAddr1 = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "epAddr2", description = "Extended Profile Address 2", type = "String", example = "Apt. 777")
	@Size(min = 0, max = 35)
	public String epAddr2 = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "epAddr3", description = "Extended Profile Address 3", type = "String", example = "Test 3 addr")
	@Size(min = 0, max = 35)
	public String epAddr3 = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "epCity", description = "Extended Profile City", type = "String", example = "Mytown")
	@Size(min = 0, max = 30)
	public String epCity = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "epState", description = "Extended Profile State", type = "String", example = "WI")
	@Size(min = 0, max = 4)
	public String epState = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "epZip", description = "Extended Profile Zip", type = "String", example = "54311")
	@Size(min = 0, max = 12)
	public String epZip = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "epCountry", description = "Extended Profile Country", type = "String", example = "USA")
	@Size(min = 0, max = 30)
	public String epCountry = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "epMinitial", description = "Extended Profile Initial", type = "String", example = "A")
	@Size(min = 0, max = 1)
	public String epMinitial = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "epTitle", description = "Extended Profile Title", type = "String", example = "Mr.")
	@Size(min = 0, max = 10)
	public String epTitle = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "epSuffix", description = "Extended Profile Suffix", type = "String", example = "Jr.")
	@Size(min = 0, max = 1)
	public String epSuffix = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "epFaxnum", description = "Extended Profile Fax Number", type = "String", example = "301-555-1212")
	@Size(min = 0, max = 24)
	public String epFaxnum = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "epMobilenum", description = "Extended Profile Mobile number", type = "String", example = "301-555-1211")
	@Size(min = 0, max = 24)
	public String epMobilenum = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "epPagernum", description = "Extended Profile Pager number", type = "String", example = "301-555-1111")
	@Size(min = 0, max = 24)
	public String epPagernum = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "epTollfreenum", description = "Extended Profile Toll Free number", type = "String", example = "301-555-2111")
	@Size(min = 0, max = 24)
	public String epTollfreenum = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "epWeburl", description = "Extended Profile Web Url", type = "String", example = "http://testweb.com")
	@Size(min = 0, max = 255)
	public String epWeburl = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "epPhotourl", description = "Extended Profile Photo Url", type = "String", example = "http://testphoto.com")
	@Size(min = 0, max = 255)
	public String epPhotourl = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "cpName1", description = "Corporate Profile Name 1", type = "String", example = "John")
	@Size(min = 0, max = 35)
	public String cpName1 = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "cpName2", description = "Corporate Profile Name 2", type = "String", example = "Doe")
	@Size(min = 0, max = 35)
	public String cpName2 = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "cpAddressLine1", description = "Corporate Profile Address Line 1", type = "String", example = "925 PRATT BLVD")
	@Size(min = 0, max = 35)
	public String cpAddressLine1 = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "cpAddressLine2", description = "Corporate Profile Address Line 2", type = "String", example = "DOCK #2 NORTH SIDE")
	@Size(min = 0, max = 35)
	public String cpAddressLine2 = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "cpAddressLine3", description = "Corporate Profile Address Line 3", type = "String", example = "1ST FLOOR")
	@Size(min = 0, max = 35)
	public String cpAddressLine3 = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "cpCity", description = "Corporate Profile City", type = "String", example = "ELK GROVE VILLAGE")
	@Size(min = 0, max = 30)
	public String cpCity = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "cpStateCd", description = "Corporate Profile State Code", type = "String", example = "IL")
	@Size(min = 0, max = 4)
	public String cpStateCd = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "cpZipCd", description = "Corporate Profile Zip Code", type = "String", example = "600075118")
	@Size(min = 0, max = 12)
	public String cpZipCd = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "cpCountryCd", description = "Corporate Profile Country Code", type = "String", example = "USA")
	@Size(min = 0, max = 3)
	public String cpCountryCd = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "cpPhoneNumber", description = "Corporate Profile Phone Number", type = "String", example = "1-888-123-1234")
	@Size(min = 0, max = 24)
	public String cpPhoneNumber = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "cpFaxNumber", description = "Corporate Profile Fax Number", type = "String", example = "1-888-123-1234")
	@Size(min = 0, max = 24)
	public String cpFaxNumber = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "cpTitle", description = "Corporate Profile Title", type = "String", example = "Manager")
	@Size(min = 0, max = 50)
	public String cpTitle = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "cpDivision", description = "Corporate Profile Division", type = "String", example = "Label Systems Division")
	@Size(min = 0, max = 30)
	public String cpDivision = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "cpDepartment", description = "Corporate Profile Department", type = "String", example = "Print Department")
	@Size(min = 0, max = 30)
	public String cpDepartment = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "cpWebUrl", description = "Corporate Profile Web URL", type = "String", example = "https://www.rrd.com")
	@Size(min = 0, max = 255)
	public String cpWebUrl = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "cpImageUrl", description = "Corporate Profile Logo URL", type = "String", example = "https://www.rrd.com/img/rrd-disclaimer-logo.png")
	@Size(min = 0, max = 255)
	public String cpImageUrl = AtWinXSConstant.EMPTY_STRING;

	// CAP-47775
	@Schema(name = "defaultAddressSourceCd", description = "Default address code, either Corporate or Extended Profile", type = "String", example = "C", allowableValues = {"", "C", "E"})
	@Size(min = 0, max = 1)
	public String defaultAddressSourceCd = AtWinXSConstant.EMPTY_STRING;

	//CAP-47448
	@Schema(name = "validatePassword", description = "A String holding the duplicate password field to validate the user typed it in correctly.", type = "String", example = "")
	public String validatePassword = AtWinXSConstant.EMPTY_STRING;

	//CAP-47617
	@Schema(name ="c1UserSiteAttributes", description = "List of site attribute being modified to the user profile", type = "array")
	private List<C1UserSiteAttribute> c1UserSiteAttributes;
	
	
	//CAP-47629
	@Schema(name ="checkWarning", description = "Flag indiciating warning checks required or not", type = "boolean", example="truee", allowableValues = {"false", "true"})
	public boolean checkWarning = true;

	//CAP-47616
	@Schema(name ="c1UserDefinedFields", description = "List of User Defined Fields being modified to the user profile", type = "array")
	private ArrayList<C1UserDefinedField> c1UserDefinedFields;
}
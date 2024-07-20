/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *  Date        Modified By     JIRA#                        Description
 *  --------    -----------     ---------------  --------------------------------
 *  02/14/24    A Boomker       CAP-46309       Pulling to base
 *	03/06/24	A Boomker		CAP-46508		Fix junits for insertion group upload
 * 	04/25/24	A Boomker		CAP-46498		Handling for lists in getUIPage API
 * 	07/01/24	A Boomker		CAP-46488		Added handling for kits
 */
package com.rrd.c1ux.api.services.custdocs;

import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Map;

import org.mockito.Mock;
import org.mockito.Mockito;

import com.rrd.c1ux.api.BaseOEServiceTest;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocBaseResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocPageBean;
import com.rrd.custompoint.customdocs.ui.CampaignUserInterfaceImpl;
import com.rrd.custompoint.customdocs.ui.DisplayControlImpl;
import com.rrd.custompoint.customdocs.ui.DisplayControlsImpl;
import com.rrd.custompoint.customdocs.ui.GroupImpl;
import com.rrd.custompoint.customdocs.ui.PageImpl;
import com.rrd.custompoint.customdocs.ui.SampleImageControlImpl;
import com.rrd.custompoint.customdocs.ui.UserInterfaceImpl;
import com.rrd.custompoint.customdocs.ui.VariableImpl;
import com.rrd.custompoint.customdocs.ui.VariablesImpl;
import com.rrd.custompoint.customdocs.ui.variables.FileUploadVariable;
import com.rrd.custompoint.framework.util.objectfactory.impl.EntityObjectMap;
import com.rrd.custompoint.orderentry.customdocs.ImprintHistorySelector;
import com.rrd.custompoint.orderentry.customdocs.ProfileSelector;
import com.rrd.custompoint.orderentry.customdocs.UIKey;
import com.rrd.custompoint.orderentry.customdocs.ui.list.UiTextListImpl;
import com.rrd.custompoint.orderentry.customdocs.ui.list.UiTextListItemImpl;
import com.rrd.custompoint.orderentry.customdocs.ui.list.UiTextListItemsImpl;
import com.rrd.custompoint.orderentry.customdocs.ui.mergelist.MergeListUploadFileImpl;
import com.rrd.custompoint.orderentry.customdocs.util.CustDocProofFormBean;
import com.rrd.custompoint.orderentry.customdocs.util.CustDocUIFormBean;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItemImpl;
import com.rrd.custompoint.orderentry.entity.ItemInstructionsImpl;
import com.rrd.custompoint.orderentry.ui.upload.UploadFileHostedResource;
import com.rrd.custompoint.orderentry.ui.upload.UploadFileImage;
import com.rrd.custompoint.orderentry.ui.upload.UploadFileInsert;
import com.rrd.custompoint.orderentry.ui.upload.UploadFileUpload;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.framework.util.Message;
import com.wallace.atwinxs.framework.util.XSProperties;
import com.wallace.atwinxs.interfaces.ICustomDocsUserInterface;
import com.wallace.atwinxs.interfaces.IManageList;
import com.wallace.atwinxs.kits.session.KitSession;
import com.wallace.atwinxs.kits.session.MKComponentInfo;
import com.wallace.atwinxs.lists.vo.ManageListsBusinessUnitPropsVO;
import com.wallace.atwinxs.orderentry.component.OEListComponent;
import com.wallace.atwinxs.orderentry.customdocs.vo.CDTemplateUserInterfaceVersionVO;
import com.wallace.atwinxs.orderentry.customdocs.vo.CDTemplateUserInterfaceVersionVOKey;
import com.wallace.atwinxs.orderentry.lists.vo.OrderListVO;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.vo.OrderLineVOKey;

abstract class BaseCustomDocsServiceTests extends BaseOEServiceTest {

	protected Map<String, String> uiRequest = null;
	protected Collection<String> errorStrings = null;

  @Mock
  protected VolatileSessionBean mockVolatileSessionBean;

  @Mock
  protected EntityObjectMap mockEntityObjectMap;

  @Mock
  protected CustomizationToken mockCustomizationToken;

  @Mock
  protected OrderEntrySession mockOrderEntrySession;

  @Mock
  protected ApplicationVolatileSession mockApplicationVolatileSession;

  @Mock
  protected UserInterfaceImpl mockUI;
  @Mock
  protected CampaignUserInterfaceImpl mockCampaignUI;

  @Mock
  protected CustomDocumentItemImpl mockItem;

  @Mock
  protected PageImpl mockPage, mockPage2;

  @Mock
  protected GroupImpl mockGroup;

  @Mock
  protected VariableImpl mockVar;
  @Mock
  protected FileUploadVariable mockFileUploadVar;

  @Mock
  protected VariablesImpl mockVariables;

  @Mock
  protected ItemInstructionsImpl mockItemInstructions;

  @Mock
  protected DisplayControlImpl mockDisplayControl, mockGroupDisplayControl, mockVariableDisplayControl;

  @Mock
  protected DisplayControlsImpl mockDisplayControls, mockGroupDisplayControls, mockVariableDisplayControls;

  @Mock
  protected SampleImageControlImpl mockSampleImageControl;

  @Mock
  protected UiTextListImpl mockUIList;

  @Mock
  protected UiTextListItemImpl mockUIListOption;

  @Mock
  protected UiTextListItemsImpl mockUIListOptions;

  @Mock
  protected CustDocUIFormBean mockUIBean;
  @Mock
  protected ICustomDocsUserInterface mockUIInterface;
  @Mock
  protected CDTemplateUserInterfaceVersionVOKey mockUIVersionKey;
  @Mock
  protected CDTemplateUserInterfaceVersionVO mockUIVersionVO;
  @Mock
  protected UIKey mockUIKey;

  @Mock
  protected OrderLineVOKey mockOrderLineVOKey;

  @Mock
  protected UploadFileImage mockUploadFileImage;
  @Mock
  protected UploadFileInsert mockUploadFileInsert;
  @Mock
  protected UploadFileHostedResource mockUploadFileHostedResource;
  @Mock
  protected UploadFileUpload mockUploadFileUpload;


  @Mock
  protected XSProperties mockXSProperties;
  @Mock
  protected Message mockMessage;
  @Mock
  protected ImprintHistorySelector mockImprintHistorySelector;

  @Mock
  protected ManageListsBusinessUnitPropsVO mockBUListSettings;

  @Mock
  protected OEListComponent mockOEListComponent;
  @Mock
  protected IManageList mockManageList;
  @Mock
  protected OrderListVO mockOrderListVO;
  @Mock
  protected MergeListUploadFileImpl mockMergeListUploadFile;
  @Mock
  protected ProfileSelector mockUserProfileSelector;

  protected CustDocProofFormBean proofBean;
  protected C1UXCustDocBaseResponse baseResponse;
  protected C1UXCustDocPageBean pageResponse;

  @Mock
  protected KitSession mockKitSession;
  @Mock
  protected MKComponentInfo mockKitComponent;

	public static final String NEW_LINE_CHAR = "\n";
	public static final String EXPECTED_403MESSAGE = ModelConstants.EXPECTED_403MESSAGE;
	protected static final String ID_NUM = "25789";
	protected static final int ID_NUMBER = 25789;
	public static final String LOGIN_ID="AMYC1UX";
	public static final String VENDOR_ITEM_NUM="74101875X1000";
	public static final String CUSTOMER_ITEM_NUM="10.1875 WIDTH";
	public static final String ITEM_DESC="Single Editable Textbox Cust Doc - For Cust Docs testing ONLY - do not add to cart in regular C1UX!";
	public static final String VAR_XML_MODIFIED="<VAR_VALUES><VAR external_source_code=\"P\" initial=\"N\" name=\"text1\" list_val=\"\" label=\"My Required Var Display label\" key_val=\"N\" type=\"T\">AmeliaPlusIModifiedThis</VAR><VAR external_source_code=\"C\" initial=\"Y\" name=\"DisplayVar\" list_val=\"\" label=\"\" key_val=\"N\" type=\"S\">TEST2</VAR></VAR_VALUES>";
	protected static final String CLASSIC_CONTEXT_PATH = "/xs2";
    protected static final String GENERIC_ERROR_MSG = "Failure message generic";

    protected static final int FAKE_GROUP_NUMBER = 24578;
    protected static final int FAKE_VAR1_NUMBER = 642;
    protected static final int FAKE_VAR2_NUMBER = 645;
    protected static final int FAKE_VAR3_NUMBER = 647;

    protected static final String FAKE_UPLOAD_LIST_VALUE = "U20946^UPLOAD: Ryan2022FallPic.jpg";

	protected void setupObjMapUploads(boolean all) {
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		Mockito.lenient().when(mockEntityObjectMap.getEntity(UploadFileImage.class, mockCustomizationToken)).thenReturn(mockUploadFileImage);
		Mockito.lenient().when(mockEntityObjectMap.getEntity(UploadFileUpload.class, mockCustomizationToken)).thenReturn(mockUploadFileUpload);
		Mockito.lenient().when(mockEntityObjectMap.getEntity(UploadFileInsert.class, mockCustomizationToken)).thenReturn(mockUploadFileInsert);
		if (all) {
			Mockito.lenient().when(mockEntityObjectMap.getEntity(UploadFileHostedResource.class, mockCustomizationToken)).thenReturn(mockUploadFileHostedResource);
		}
	}

}



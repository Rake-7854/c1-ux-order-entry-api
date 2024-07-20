package com.rrd.c1ux.api.models.orders.ordersearch;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="COOrderEmailDetails", description = "Class for an individual email detail result",type = "object")
public class COOrderEmailDetails {
	@Schema(name ="emailCode", description = "Email code in CustomPoint", type = "string", example="ROUTE1")
	public String emailCode;
	@Schema(name ="emailDescription", description = "Based on the Email code the assign the decription", type = "string", example="Routed Order Email")
	public String emailDescription;
	@Schema(name ="emailTime", description = "Email time in CustomPoint", type = "string", example="12/14/2022 09:34:16")
	public String emailTime;
	@Schema(name ="linkUrl", description = "Link URL in CustomPoint", type = "string", example="https://dev.custompoint.rrd.com/cp/orderstatus/orderemail/ajax.cp?ttsessionid=V2g1cEQ4bFR5eHE5Sys3cWdiMHJtV0x1RjVBNlBEbVFYOXBVY3dHVFdPZFQzQTZqVjFrbjhRPT0%3D&eventID=EVENT_VIEW_EMAIL&a=p8nGvNpjwbHyvthcGVbg5nvIATcd3wTIQ9sbia6KRCPcvhGUdky4qB4CJuWKxLjlMGKvC2fTXQ4c05ugDYfS0MDH3u9YrMQ5sbOvWiEj5o32Hqe1B5xmQ%2BxmYSv3oFwMbIyKEhtAaE5H9SfXD0Q30tCkHaViUSllJM%2F%2FFOAHFOyY51DfExMc6%2FlXBiKS9hHEVztp2YutDBEy03tILLK0qBsjdjPlJWtfNoOnk8jv9HMcS5FI7Om%2BIihqeBf792oXdfA9Qpy4NDgyWG5JuygacmRWdBybIrGg1Kp2yAgmjPEmePR4oun93DGiYo%2F9VxoPQCfBSB9DI9uzyiSXlLksKi4Jk%2BKfMLQ8mMv6NAW9mC4%3D")
	public String linkUrl;
}

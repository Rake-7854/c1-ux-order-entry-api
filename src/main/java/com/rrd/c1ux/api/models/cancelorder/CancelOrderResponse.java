package com.rrd.c1ux.api.models.cancelorder;



import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(name ="CancelOrderResponse", description = "Response Class for Cancel order", type = "object")
public class CancelOrderResponse extends BaseResponse{

}

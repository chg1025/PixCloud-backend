package com.chg.pixCloud.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class SearchPictureByPictureRequest implements Serializable {

    private static final long serialVersionUID = 8492753698319242699L;

    /**
     * 图片 id
     */
    private Long pictureId;


}

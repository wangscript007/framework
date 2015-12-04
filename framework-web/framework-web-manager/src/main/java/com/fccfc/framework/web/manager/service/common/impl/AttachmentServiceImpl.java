package com.fccfc.framework.web.manager.service.common.impl;

import com.fccfc.framework.common.ErrorCodeDef;
import com.fccfc.framework.common.ServiceException;
import com.fccfc.framework.common.utils.io.ImageUtil;
import com.fccfc.framework.common.utils.logger.Logger;
import com.fccfc.framework.config.core.ConfigHelper;
import com.fccfc.framework.db.core.DaoException;
import com.fccfc.framework.message.core.bean.AttachmentsPojo;
import com.fccfc.framework.web.manager.dao.common.AttachmentsDao;
import com.fccfc.framework.web.manager.service.common.AttachmentService;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;

/**
 * Created by wk on 2015/10/20.
 */
@Service
public class AttachmentServiceImpl implements AttachmentService {
    private static Logger logger = new Logger(AttachmentServiceImpl.class);
    @Resource
    private AttachmentsDao attachmentsDao;

    /*
     * (non-Javadoc)
     * @see com.fccfc.framework.web.service.ResourceService#downloadResource(int)
     */
    @Override
    public AttachmentsPojo downloadResource(int resourceId, boolean isThumb) throws ServiceException {
        try {
            String thumbPath = null;
            AttachmentsPojo pojo = attachmentsDao.selectAttachments(resourceId);
            if (pojo == null) {
                throw new ServiceException(ErrorCodeDef.RESOURCE_ID_ERROR_20003, "未找到resourceId为{0}的资源", resourceId);
            }

            String resourcePath = ConfigHelper.getString("RESOURCE.PATH");

            File file = new File(resourcePath + pojo.getFilePath());
            if (!file.exists()) {
                throw new ServiceException(ErrorCodeDef.RESOURCE_ID_ERROR_20003, "未找到resourceId为{0}的资源", resourceId);
            }

            if (isThumb && "Y".equals(pojo.getIsPicture())) {
                if (!"Y".equals(pojo.getIsThumb())) {
                    thumbPath = new StringBuilder(pojo.getFilePath())
                            .insert(pojo.getFilePath().lastIndexOf("."), "_thumb").toString();

                    try {
                        ImageUtil.pictureZoom(resourcePath + pojo.getFilePath(), resourcePath + thumbPath);
                        if (thumbPath.toLowerCase().indexOf("jpg") == -1
                                || thumbPath.toLowerCase().indexOf("jpeg") == -1) {
                            thumbPath = thumbPath.substring(0, thumbPath.lastIndexOf(".")) + ".jpg";
                        }
                    } catch (Exception e) {
                        logger.warn("生成缩略图失败", e);
                        thumbPath = pojo.getFilePath();
                    }
                    pojo.setThumbPath(thumbPath);
                }
            }
            attachmentsDao.updateAttachments(resourceId, thumbPath);

            return pojo;
        } catch (DaoException e) {
            throw new ServiceException(e);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.fccfc.framework.web.service.ResourceService#saveAttachment(com.fccfc
     * .framework.core.bean.resource.AttachmentsPojo )
     */
    @Override
    public void saveAttachment(AttachmentsPojo attachments) throws ServiceException {
        try {
            attachmentsDao.save(attachments);
        } catch (DaoException e) {
            throw new ServiceException(e);
        }
    }
}
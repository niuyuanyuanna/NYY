package com.liuyuan.nyy.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.Surface;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 摄像头工具类，用于保存图片，设置摄像头参数
 */
public class CameraHelper1 {
    private final static String TAG = CameraHelper1.class.getSimpleName();

    private static CameraHelper1 instance;
    // 图片压缩质量
    private static int JPEGQuality = 80;
    // 原始照片数据
    private byte[] mOriginalPicData;
    // 旋转纠正过后的图片
    private Bitmap mCorrectedBitmap;

    public static CameraHelper1 createHelper(Context context) {
        if (null == instance) {
            instance = new CameraHelper1(context);
        }
        return instance;
    }


    private CameraHelper1(Context context) {
//        computeContentSize(context);
    }

    /**
     * 实现相机预览支持分辨率的升序排列
     */
    public class CameraSizeComparator implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            if (lhs.width == rhs.width) {
                return 0;
            } else if (lhs.width > rhs.width) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    /**
     * 从sizeList中找到最大的size
     *
     * @return 最大的size
     */
    private Size getMaxSize(List<Size> sizeList) {
        CameraSizeComparator sizeComparator = new CameraSizeComparator();
        Collections.sort(sizeList, sizeComparator);// 拍照图片大小排序

        return sizeList.get(sizeList.size() - 1);
    }

    /**
     * 从sizeList中找到与屏幕长宽比最接近且最大的size
     *
     * @return 没找到则返回null
     */
    private Size getMaxFitSize(List<Size> sizeList, int screenWidth, int screenHeight) {
        CameraSizeComparator sizeComparator = new CameraSizeComparator();
        Collections.sort(sizeList, sizeComparator);// 拍照图片大小排序

        // 保证width > height
        if (screenWidth < screenHeight) {
            int tmp = screenWidth;
            screenWidth = screenHeight;
            screenHeight = tmp;
        }

        double ratio = (double) screenWidth / screenHeight;
        for (Size size : sizeList) {
            int width = size.width;
            int height = size.height;
            // 保证width > height
            if (width < height) {
                int tmp = width;
                width = height;
                height = tmp;
            }
            // 网上说比例之差在0.03之间最适合
            double ratio2 = (double) width / height;
            if (Math.abs(ratio - ratio2) < 0.13) {
                return size;
            }
        }

        return null;
    }

    /**
     * 获取最适合屏幕分辨率的预览大小
     */
    public Size getFitPreviewSize(List<Size> previewlist,
                                  List<Size> picturelist, int screenWidth, int screenHeight) {
        CameraSizeComparator sizeComparator = new CameraSizeComparator();
        Collections.sort(previewlist, sizeComparator);// 预览大小排序从小到大
        Collections.sort(picturelist, sizeComparator);// 拍照图片大小排序

        List<Size> commonsize = new ArrayList<Size>();// 共同支持的分辨率
        for (int i = 0; i < picturelist.size(); i++) {
            for (int j = 0; j < previewlist.size(); j++) {
                if ((picturelist.get(i).width == previewlist.get(j).width)
                        && picturelist.get(i).height == previewlist.get(j).height) {
                    commonsize.add(picturelist.get(i));
                }
            }
        }

        Size maxSize = getMaxFitSize(previewlist, screenWidth, screenHeight);
        if (null != maxSize) {
            return maxSize;
        }

        return commonsize.get(commonsize.size() - 1);
    }

    /**
     * 获得合适预览界面
     *
     * @param previewlist
     * @param picturelist
     * @return
     */
    public Size getPreviewSize(List<Size> previewlist,
                               List<Size> picturelist) {
        CameraSizeComparator sizeComparator = new CameraSizeComparator();
        Collections.sort(previewlist, sizeComparator);// 预览大小排序从小到大
        Collections.sort(picturelist, sizeComparator);// 图片大小排序

        List<Size> commonsize = new ArrayList<Size>();// 共同支持的分辨率
        for (int i = 0; i < picturelist.size(); i++) {
            for (int j = 0; j < previewlist.size(); j++) {
                if ((picturelist.get(i).width == previewlist.get(j).width)
                        && picturelist.get(i).height == previewlist.get(j).height) {
                    commonsize.add(picturelist.get(i));
                }
            }
        }
        return commonsize.get(commonsize.size() - 1);
    }

    /**
     * 获取摄相头参数设置
     * @param context 上下文
     * @param camera  摄像头对象
     * @return
     */
    public Camera.Parameters getCameraParam(Context context, Camera camera) {
        Camera.Parameters params = camera.getParameters();
        // 设置图片格式
        params.setPictureFormat(PixelFormat.JPEG);
        // 设置照片质量
        params.setJpegQuality(80);

        List<Size> previewSizes = params.getSupportedPreviewSizes();
        List<Size> pictureSizes = params.getSupportedPictureSizes();

        Size cameraSize =getPreviewSize(previewSizes, pictureSizes);

        // 自动聚焦,后置镜头可以自动对焦但是前置不行，需要硬件的支持
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        if (null == cameraSize) {
            return params;
        }
        // 设置保存的图片尺寸
        params.setPictureSize(cameraSize.width, cameraSize.height);
//        params.setPictureSize(1280, 720);
        // 设置预览大小
        params.setPreviewSize(cameraSize.width, cameraSize.height);
//        params.setPreviewSize(1280, 720);

        return params;
    }

    /**
     * 获取摄相头参数设置
     *
     * @param context      上下文
     * @param camera       摄像头对象
     * @param screenWidth  屏幕宽
     * @param screenHeight 屏幕高
     * @return
     */
    public Camera.Parameters getCameraParam(Context context, Camera camera,
                                            int screenWidth, int screenHeight) {
        Camera.Parameters params = camera.getParameters();
        // 设置图片格式
        params.setPictureFormat(PixelFormat.JPEG);
        // 设置照片质量
        params.setJpegQuality(80);

        List<Size> previewSizes = params.getSupportedPreviewSizes();
        List<Size> pictureSizes = params.getSupportedPictureSizes();

        Size cameraSize = getFitPreviewSize(previewSizes, pictureSizes,
                screenWidth, screenHeight);
        Size maxPictureSize = getMaxSize(pictureSizes);

        // 自动聚焦,后置镜头可以自动对焦但是前置不行，需要硬件的支持
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        if (null == cameraSize) {
            return params;
        }

        params.setPreviewSize(cameraSize.width, cameraSize.height);        // 设置预览大小

        if (null != maxPictureSize) {
            params.setPictureSize(maxPictureSize.width, maxPictureSize.height); // 设置图片尺寸
        } else {
            params.setPictureSize(cameraSize.width, cameraSize.height); // 设置图片尺寸
        }

        return params;
    }

    /**
     * 将字节数组的图形数据转换为Bitmap
     * 将图像像素变为原来的1/4
     *
     * @return Bitmap格式的图片
     */
    private Bitmap byte2Bitmap() {
        try {
            // 将图像像素变为原来的1/4
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = 2;
            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
            opts.inPurgeable = true;
            opts.inInputShareable = true;

            Bitmap bitmap = BitmapFactory.decodeByteArray(mOriginalPicData, 0,
                    mOriginalPicData.length, opts);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 存入图片数据（临时数据）
     * @param data    图片数据
     * @param context 上下文
     */
    public void setCacheData(byte[] data, Context context) {

        try {
            if (null != data) {
                mOriginalPicData = data;
            }

            //将原始照片转化为像素渣的bitmap
            Bitmap bitmap = byte2Bitmap();

            Matrix matrix = new Matrix();
            matrix.postRotate((float) 0.0);

            recycleCacheBitmap();
            mCorrectedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            recycleBitmap(bitmap);
//			bitmap = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取旋转纠正后的图片数据，用于存储文件
     * @return
     */
    public Bitmap getImageBitmap() {
        return mCorrectedBitmap;
    }

    /**
     * 将旋转纠正后的图片数据进行等比缩放，得到缩放后的图片
     * （等比放缩到800*600）
     * @return byte
     */
    public byte[] getImageData() {
        ByteArrayOutputStream baos = null;
        try {
            Matrix matrix = new Matrix();
            // 获取原始图片的宽和高度
            int orgWidth = mCorrectedBitmap.getWidth();
            int orgHeight = mCorrectedBitmap.getHeight();
            float scaleWidth = ((float) orgWidth) / 600;
            float scaleHeigth = ((float) orgHeight) / 800;
            float scale = Math.max(scaleWidth, scaleHeigth);
            matrix.postScale(1 / scale, 1 / scale);

            // 得到放缩后的图片
            Bitmap scaledBitmap = Bitmap.createBitmap(mCorrectedBitmap, 0, 0, orgWidth, orgHeight, matrix, true);
            Log.d(TAG, "Image orgwidth:" + orgWidth + ",scaleWidth:" + scaleWidth
                    + ",orgHeight:" + orgHeight + ",scaleHeigth:" + scaleHeigth
                    + ",newWidth:" + scaledBitmap.getWidth() + ",newHeight:"
                    + scaledBitmap.getHeight());

            // 将调整后的图片转换成字节流
            baos = new ByteArrayOutputStream();
            byte[] bytes = null;

            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, JPEGQuality, baos);
            bytes = baos.toByteArray();

            recycleBitmap(scaledBitmap);
//			scaledBitmap = null;
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                }
//				baos = null;
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 释放bitmap资源
     *
     * @param bitmap
     */
    private void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
//			bitmap = null;
        }
    }

    /**
     * 释放缓存的bitmap
     */
    private void recycleCacheBitmap() {
        recycleBitmap(mCorrectedBitmap);
        mCorrectedBitmap = null;
        System.gc();
    }
}
package com.liuyuan.nyy.ui;

import com.nightonke.blurlockview.Directions.HideType;
import com.nightonke.blurlockview.Directions.ShowType;
import com.nightonke.blurlockview.Eases.EaseType;

/**
 * Created by admin on 2017/3/29.
 */

public class BlurLockType {


    public ShowType getShowType(int p) {
        ShowType showType = ShowType.FROM_TOP_TO_BOTTOM;
        switch (p) {
            case 0:
                showType = ShowType.FROM_TOP_TO_BOTTOM;
                break;
            case 1:
                showType = ShowType.FROM_RIGHT_TO_LEFT;
                break;
            case 2:
                showType = ShowType.FROM_BOTTOM_TO_TOP;
                break;
            case 3:
                showType = ShowType.FROM_LEFT_TO_RIGHT;
                break;
            case 4:
                showType = ShowType.FADE_IN;
                break;
        }
        return showType;
    }

    public HideType getHideType(int p) {
        HideType hideType = HideType.FROM_TOP_TO_BOTTOM;
        switch (p) {
            case 0:
                hideType = HideType.FROM_TOP_TO_BOTTOM;
                break;
            case 1:
                hideType = HideType.FROM_RIGHT_TO_LEFT;
                break;
            case 2:
                hideType = HideType.FROM_BOTTOM_TO_TOP;
                break;
            case 3:
                hideType = HideType.FROM_LEFT_TO_RIGHT;
                break;
            case 4:
                hideType = HideType.FADE_OUT;
                break;
        }
        return hideType;
    }

    public EaseType getEaseType(int p) {
        EaseType easeType = EaseType.Linear;
        switch (p) {
            case 0:
                easeType = EaseType.EaseInSine;
                break;
            case 1:
                easeType = EaseType.EaseOutSine;
                break;
            case 2:
                easeType = EaseType.EaseInOutSine;
                break;
            case 3:
                easeType = EaseType.EaseInQuad;
                break;
            case 4:
                easeType = EaseType.EaseOutQuad;
                break;
            case 5:
                easeType = EaseType.EaseInOutQuad;
                break;
            case 6:
                easeType = EaseType.EaseInCubic;
                break;
            case 7:
                easeType = EaseType.EaseOutCubic;
                break;
            case 8:
                easeType = EaseType.EaseInOutCubic;
                break;
            case 9:
                easeType = EaseType.EaseInQuart;
                break;
            case 10:
                easeType = EaseType.EaseOutQuart;
                break;
            case 11:
                easeType = EaseType.EaseInOutQuart;
                break;
            case 12:
                easeType = EaseType.EaseInQuint;
                break;
            case 13:
                easeType = EaseType.EaseOutQuint;
                break;
            case 14:
                easeType = EaseType.EaseInOutQuint;
                break;
            case 15:
                easeType = EaseType.EaseInExpo;
                break;
            case 16:
                easeType = EaseType.EaseOutExpo;
                break;
            case 17:
                easeType = EaseType.EaseInOutExpo;
                break;
            case 18:
                easeType = EaseType.EaseInCirc;
                break;
            case 19:
                easeType = EaseType.EaseOutCirc;
                break;
            case 20:
                easeType = EaseType.EaseInOutCirc;
                break;
            case 21:
                easeType = EaseType.EaseInBack;
                break;
            case 22:
                easeType = EaseType.EaseOutBack;
                break;
            case 23:
                easeType = EaseType.EaseInOutBack;
                break;
            case 24:
                easeType = EaseType.EaseInElastic;
                break;
            case 25:
                easeType = EaseType.EaseOutElastic;
                break;
            case 26:
                easeType = EaseType.EaseInOutElastic;
                break;
            case 27:
                easeType = EaseType.EaseInBounce;
                break;
            case 28:
                easeType = EaseType.EaseOutBounce;
                break;
            case 29:
                easeType = EaseType.EaseInOutBounce;
                break;
            case 30:
                easeType = EaseType.Linear;
                break;
        }
        return easeType;
    }

}

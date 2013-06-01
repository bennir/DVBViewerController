package de.bennir.DVBViewerController;

import android.content.Context;
import de.bennir.DVBViewerController.wizard.model.*;

public class TimerWizardModel extends AbstractWizardModel {

    public TimerWizardModel(Context context) {
        super(context);
    }

    @Override
    protected PageList onNewRootPageList() {
        return new PageList(
                new SingleFixedChoicePage(this, "channel")
                        .setChoices(DVBViewerControllerActivity.chanNames)
                        .setRequired(true),
                new TimerInfoPage(this, "timer_info", mContext)
                        .setRequired(true),
                new TimerDatePage(this, "timer_date")
                        .setRequired(true),
                new SingleFixedChoicePage(this, "timer_action")
                        .setChoices(mContext.getString(R.string.record), mContext.getString(R.string.set_channel)),
                new SingleFixedChoicePage(this, "timer_after")
                        .setChoices(mContext.getString(R.string.power_off), mContext.getString(R.string.standby), mContext.getString(R.string.hibernate))
        );
    }
}

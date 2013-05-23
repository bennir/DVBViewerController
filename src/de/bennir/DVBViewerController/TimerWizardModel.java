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
                new SingleFixedChoicePage(this, "Channel")
                        .setChoices(DVBViewerControllerActivity.chanNames)
                        .setRequired(true),

                new TimerInfoPage(this, "Timer info")
                        .setRequired(true)
        );
    }
}

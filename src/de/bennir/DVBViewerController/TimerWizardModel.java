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
                new TimerInfoPage(this, "Timer Info")
                        .setRequired(true),
                new TimerDatePage(this, "Timer Date")
                        .setRequired(true),
                new SingleFixedChoicePage(this, "Timer Action")
                        .setChoices("Record", "Tune"),
                new SingleFixedChoicePage(this, "After Timer")
                        .setChoices("Power Off", "Standby", "Hibernate")
        );
    }
}

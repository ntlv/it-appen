package it.sektionen.android.itappen.fragments;

import it.sektionen.android.itappen.ITappen;
import it.sektionen.android.itappen.MainActivity;
import it.sektionen.android.itappen.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ClassChoiceDialogFragment extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Välj en klass").setItems(R.array.class_string_array,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Context ctx = ITappen.getContext();
						if (ctx != null) {
							ctx.getSharedPreferences(MainActivity.PREFERENCES,
									Context.MODE_PRIVATE)
									.edit()
									.putInt(ScheduleFragment.SELECTED_SCHEDULE,
											which).apply();


						}
					}
				});
		return builder.create();
	}
}

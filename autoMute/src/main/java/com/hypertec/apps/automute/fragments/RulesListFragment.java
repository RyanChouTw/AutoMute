package com.hypertec.apps.automute.fragments;

import java.util.ArrayList;
import java.util.Calendar;

import com.hypertec.apps.automute.R;
import com.hypertec.apps.automute.RuleEditorActivity;
import com.hypertec.apps.automute.provider.Rule;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;
import it.gmariotti.cardslib.library.view.listener.UndoBarController;
import it.gmariotti.cardslib.library.view.listener.dismiss.DefaultDismissableManager;

public class RulesListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private ArrayList<Card> mRulesCards;
    private CardArrayAdapter mRulesCardArrayAdapter;
    private CardListView mRulesCardListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		getLoaderManager().initLoader(0, null, this);
		setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_rules_list, container, false);
		return v;
    }
    
    private void initRulesCards(Cursor cursor) {
		int numRules = cursor.getCount();
		cursor.moveToFirst();
	
		mRulesCards = new ArrayList<Card>();
		for (int i = 0; i < numRules; i++) {
		    RuleCard card = new RuleCard(this.getActivity());
		    final Rule rule = new Rule(cursor);
		    card.init(rule);
		    mRulesCards.add(card);
		    cursor.moveToNext();
		}
	
		if (mRulesCardArrayAdapter != null) {
		    mRulesCardArrayAdapter.clear();
		    mRulesCardArrayAdapter.addAll(mRulesCards);
		}
		else {
            mRulesCardArrayAdapter = new CardArrayAdapter(getActivity(), mRulesCards);
            mRulesCardArrayAdapter.setDismissable(new RightDismissableManger());
            mRulesCardListView = (CardListView) getActivity().findViewById(R.id.rules_list_card);
            if (mRulesCardListView!=null) {
                mRulesCardListView.setAdapter(mRulesCardArrayAdapter);
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
	    return Rule.getRulesCursorLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            initRulesCards(data);
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    	mRulesCards.clear();
    }
      
    private void startRuleEditor(View view, Rule rule) {
        Intent intent = new Intent(getActivity().getBaseContext(), RuleEditorActivity.class);

        intent.putExtra(Rule.RULE_ITEM, rule);
        getActivity().startActivity(intent);
	
    }

    public class RightDismissableManger extends DefaultDismissableManager {
        @Override
        public SwipeDirection getSwipeDirectionAllowed() {
            return SwipeDirection.RIGHT;
        }
    }

    /**
     * This class provides a card presenting Rules
     * 
     *  @author Ryan Chou (ryanchou0210@gmail.com)
     */

    public class RuleCard extends Card {

		protected Rule mRule;
	
		protected int resIdCheckedThumb = R.drawable.ic_checkbox_checked;
		protected int resIdUnchekedThumb = R.drawable.ic_checkbox_uncheck;
		protected int resIdVirateThumb = R.drawable.ic_vibrate;

		private final int[] WeekDayViewID = {
			R.id.rule_card_repeat_weekday_monday,
			R.id.rule_card_repeat_weekday_tuesday,
			R.id.rule_card_repeat_weekday_wednesday,
			R.id.rule_card_repeat_weekday_thursday,
			R.id.rule_card_repeat_weekday_friday,
			R.id.rule_card_repeat_weekday_saturday,
			R.id.rule_card_repeat_weekday_sunday,
		};

		private final int[] DAY_ORDER = new int[] {
			Calendar.MONDAY,
			Calendar.TUESDAY,
			Calendar.WEDNESDAY,
			Calendar.THURSDAY,
			Calendar.FRIDAY,
			Calendar.SATURDAY,
			Calendar.SUNDAY,
		};
	
		public RuleCard(Context context) {
		    this(context, R.layout.rulecard_inner_content);
		}

		public RuleCard(Context context, int innerLayout) {
		    super(context, innerLayout);
		}

		@Override
		public void setupInnerViewElements(ViewGroup parent, View view) {
		    // Retrieve elements
		    TextView titleView = (TextView) parent.findViewById(R.id.rule_card_title);
		    TextView periodView = (TextView) parent.findViewById(R.id.rule_card_period);
	
		    if (titleView != null) {
		    	titleView.setText(mRule.getTitleOrDefault(mContext));

				if (mRule.isVibrate())
				    titleView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_vibrate, 0);
				else
				    titleView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			    }

			    if (periodView != null)
					periodView.setText(buildPeriodDispText());
	
			    for (int i = 0; i < DAY_ORDER.length; i++) {
					TextView weekDayView = (TextView) parent.findViewById(WeekDayViewID[i]);
					if (mRule.getRepeat().isDaysOfWeekEnabled(DAY_ORDER[i])) {
					    weekDayView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.ic_checkbox_checked);
				}
		    }
		}

		private CharSequence buildPeriodDispText() {
	    
		    CharSequence start = mRule.getStartTimeDispText(mContext);
		    CharSequence end = mRule.getEndTimeDispText(mContext);
	
		    return start + " ~ " + end;
		}
	
		/*
		 * Public Functions
		 */
		public String getTitle() {
		    return mRule.getTitleOrDefault(mContext);
		}
	
		public int getResIdCheckedThumb() {
		    return resIdCheckedThumb;
		}
	
		public void setResIdCheckedThumb(int resId) {
		    this.resIdCheckedThumb = resId;
		}
	
		public int getResIdUncheckedThumb() {
		    return resIdUnchekedThumb;
		}
	
		public void setResIdUncheckedThumb(int resId) {
		    this.resIdUnchekedThumb = resId;
		}
	
		public int getResIdVibrateThumb() {
		    return resIdVirateThumb;
		}
	
		public void setResIdVibrateThumb(int resId) {
		    this.resIdVirateThumb = resId;
		}

		public void init(Rule rule) {
            this.mRule = rule;

            /*
                        * active : means the muter is working now
                        * inactive : means the muter is enabled, but not working now
                        * off : means the muter is disable
                        */
            if (rule.isActive()) {
                // means the current Time is located between start time and end time
                setBackgroundResourceId(R.drawable.card_active_background_color);
            }
            else if (rule.isEnabled() == true) {
                setBackgroundResourceId(R.drawable.card_inactive_background_color);
            } else {
                setBackgroundResourceId(R.drawable.card_off_background_color);
            }

            // swipe-able
            setSwipeable(true);
            setId(Integer.toString((int) rule.getId()));
            setOnSwipeListener(new Card.OnSwipeListener() {
                @Override
                public void onSwipe(Card in) {
                    // delete the rule
                    RuleCard ruleCard = (RuleCard) in;
                    Rule rule = ruleCard.getRule();
                    String message = getResources().getQuantityString(R.plurals.rule_list_card_undo_items, 1, 1);
                    Rule.deleteRule(getActivity().getContentResolver(), rule);
                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                }
            });
            setOnClickListener(new Card.OnCardClickListener() {
                @Override
                public void onClick(Card in, View view) {
                    RuleCard card = (RuleCard) in;
                    Rule rule = card.getRule();
                    startRuleEditor(view, rule);
                }
            });
        }
		public Rule getRule() {
		    return mRule;
		}
    }
}


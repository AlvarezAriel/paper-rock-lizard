//
// Copyright 2016 by Grindr LLC,
// All rights reserved.
//
// This software is confidential and proprietary information of
// Grindr LLC ("Confidential Information").
// You shall not disclose such Confidential Information and shall use
// it only in accordance with the terms of the license agreement
// you entered into with Grindr LLC.
//
package avion.piedrapapellagarto;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.Random;

import avion.piedrapapellagarto.model.GameChoice;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static avion.piedrapapellagarto.model.GameChoice.ALL_CHOICES;

public class GameFragment extends Fragment {


    @BindView(R.id.match_result)
    View matchResult;

    @BindView(R.id.oponent_choice)
    ImageView oponentChoiceView;

    @BindView(R.id.my_choice)
    ImageView myChoiceView;

    GameChoice selectedChoice;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @OnClick(R.id.rock)     public void selectRock()    { onUserSelection(GameChoice.ROCK);    }
    @OnClick(R.id.paper)    public void selectPaper()   { onUserSelection(GameChoice.PAPER);   }
    @OnClick(R.id.scissors) public void selectScissors(){ onUserSelection(GameChoice.SCISSORS);}
    @OnClick(R.id.spock)    public void selectSpock()   { onUserSelection(GameChoice.SPOCK);   }
    @OnClick(R.id.lizard)   public void selectLizard()  { onUserSelection(GameChoice.LIZARD);  }
    
    public void onUserSelection(GameChoice gameChoice) {
        GameChoice oponentChoice = fetchRandomChoice();
        selectedChoice = gameChoice;

        oponentChoiceView.setImageResource(oponentChoice.drawable);
        myChoiceView.setImageResource(selectedChoice.drawable);


        if (oponentChoice.getStrongerAgainst().contains(selectedChoice)) {
            matchResult.setBackgroundResource(R.color.colorLoose);
        } else if (selectedChoice.getStrongerAgainst().contains(oponentChoice)){
            matchResult.setBackgroundResource(R.color.colorWin);
        } else {
            matchResult.setBackgroundResource(R.color.colorUndefined);
        }
    }

    private GameChoice fetchRandomChoice() {
        return ALL_CHOICES.get(new Random().nextInt(ALL_CHOICES.size()));
    }
}

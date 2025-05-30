package com.antonelli.gibim.degolabot;

import com.bueno.spi.model.CardToPlay;
import com.bueno.spi.model.GameIntel;
import com.bueno.spi.model.TrucoCard;
import com.antonelli.gibim.degolabot.BotUtils;

import java.util.Comparator;
import java.util.List;

public class FirstRound implements Strategy {

    @Override
    public int getRaiseResponse(GameIntel intel) {
        boolean opponentHasCard = BotUtils.isPlayingSecond(intel);
        List<TrucoCard> currentCards = intel.getCards();

        if (opponentHasCard && intel.getOpponentCard().isPresent() &&
                currentCards.stream()
                        .filter(c -> c.compareValueTo(intel.getOpponentCard().get(), intel.getVira()) >= 0)
                        .count() >= 2) {
            return 0;
        }

        if (intel.getOpponentScore() < 6 && BotUtils.countStrongCards(intel) > 0) {
            return 0;
        }

        boolean hasZap = BotUtils.cards(intel).anyMatch(c -> c.isZap(intel.getVira()));
        boolean hasManilha = BotUtils.cards(intel).anyMatch(c -> c.isManilha(intel.getVira()) && !c.isZap(intel.getVira()));
        if (hasZap && hasManilha) return 1;

        if (BotUtils.countManilha(intel) > 0) return 0;

        if (BotUtils.countStrongCards(intel, 8) >= 2) return 0;

        return -1;
    }

    @Override
    public boolean decideIfRaises(GameIntel intel) {
        if (BotUtils.countManilha(intel) >= 2 || BotUtils.countStrongCards(intel) > 2) return true;

        if (intel.getOpponentCard().isPresent()) {
            TrucoCard opponentCard = intel.getOpponentCard().get();
            boolean canBeat = intel.getCards().stream()
                    .anyMatch(c -> c.compareValueTo(opponentCard, intel.getVira()) > 0);

            if (canBeat || BotUtils.countStrongCards(intel, 7) >= 2) return true;

            return intel.getCards().stream()
                    .filter(c -> c.compareValueTo(opponentCard, intel.getVira()) >= 0)
                    .count() >= 2 && BotUtils.countStrongCards(intel, 8) > 0;
        }

        return false;
    }

    @Override
    public CardToPlay chooseCard(GameIntel intel) {
        boolean isSecond = BotUtils.isPlayingSecond(intel);

        if (!isSecond) {
            return playAsFirst(intel);
        } else {
            return playAsSecond(intel);
        }
    }

    private CardToPlay playAsFirst(GameIntel intel) {
        if (BotUtils.hasOnlyWeakCards(intel)) {
            return CardToPlay.of(BotUtils.selectWeakestCard(intel));
        }

        if (BotUtils.hasOnlyMediumCards(intel)) {
            return CardToPlay.of(BotUtils.selectStrongestCard(intel));
        }

        if (BotUtils.hasOnlyStrongCards(intel)) {
            return CardToPlay.of(BotUtils.selectStrongestCard(intel));
        }

        if (BotUtils.hasManilhaAndStrongCard(intel)) {
            return CardToPlay.of(BotUtils.selectCardGreaterThanTwo(intel));
        }

        if (BotUtils.hasManilhaAndTwoWeakCards(intel)) {
            return CardToPlay.of(BotUtils.findManilha(intel).orElse(BotUtils.selectStrongestCard(intel)));
        }

        return CardToPlay.of(BotUtils.selectStrongestCard(intel));
    }


    private CardToPlay playAsSecond(GameIntel intel) {
        List<TrucoCard> cards = intel.getCards();
        TrucoCard adversario = intel.getOpponentCard().get();

        long boas = BotUtils.countStrongCards(intel, 8);
        long manilhas = BotUtils.countManilha(intel);
        long ruins = BotUtils.countWeakCards(intel);
        long medias = BotUtils.countMediumCards(intel);

        if (ruins == 3 || medias >= 1 || boas >= 3) {
            TrucoCard matadora = BotUtils.findWeakestWinningCard(cards, adversario, intel);
            return CardToPlay.of(matadora != null ? matadora : BotUtils.selectWeakestCard(intel));
        }

        if (manilhas >= 1) {
            if (boas >= 1) {
                TrucoCard matadora = BotUtils.findWeakestWinningCard(cards, adversario, intel);
                if (matadora != null) return CardToPlay.of(matadora);

                TrucoCard amarradora = BotUtils.findTyingCard(cards, adversario, intel);
                if (amarradora != null) return CardToPlay.of(amarradora);

                return CardToPlay.of(BotUtils.findManilha(intel).orElse(BotUtils.selectStrongestCard(intel)));
            }

            if (ruins >= 1) {
                TrucoCard amarradora = BotUtils.findTyingCard(cards, adversario, intel);
                if (amarradora != null) return CardToPlay.of(amarradora);

                return CardToPlay.of(BotUtils.findManilha(intel).orElse(BotUtils.selectStrongestCard(intel)));
            }
        }

        return CardToPlay.of(BotUtils.selectStrongestCard(intel));
    }

}
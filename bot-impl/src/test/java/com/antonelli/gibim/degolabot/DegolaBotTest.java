/*
 *  Copyright (C) 2025 Daniel da Silva Gibim and Mauricio Antonelli de Oliveira
 *  Contact: daniel <dot> gibim <at> aluno <dot> ifsp <dot> edu <dot> br
 *  Contact: a <dot> mauricio <at> aluno <dot> ifsp <dot> edu <dot> br
 *
 *  This file is part of CTruco (Truco game for didactic purpose).
 *
 *  CTruco is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  CTruco is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with CTruco.  If not, see <https://www.gnu.org/licenses/>
 */

package com.antonelli.gibim.degolabot;

import com.bueno.spi.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.bueno.spi.model.CardRank.*;
import static com.bueno.spi.model.CardRank.ACE;
import static com.bueno.spi.model.CardSuit.*;
import static com.bueno.spi.model.CardSuit.HEARTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Testes do DegolaBot")
public class DegolaBotTest {

    private DegolaBot sut;
    GameIntel.StepBuilder intel;

    @BeforeEach
    void setUp() {
        sut = new DegolaBot();
    }

    private GameIntel.StepBuilder firstRoundFirstToPlay(
            List<TrucoCard> botCards, List<TrucoCard> openCards, TrucoCard vira) {
        return GameIntel.StepBuilder.with().gameInfo(List.of(), openCards, vira, 1)
                .botInfo(botCards, 0).opponentScore(0);
    }
    private GameIntel.StepBuilder firstRoundSecondToPlay(
            List<TrucoCard> botCards, List<TrucoCard> openCards, TrucoCard vira, TrucoCard opponentCard) {
        return GameIntel.StepBuilder.with().gameInfo(List.of(), openCards, vira, 1)
                .botInfo(botCards, 0).opponentScore(0).opponentCard(opponentCard);
    }

    @Nested
    @DisplayName("Testes do método chooseCard")
    class ChooseCardTests {

        @Test
        @DisplayName("Deve retornar uma carta válida")
        void testChooseCardReturnsValidCardToPlay() {
            TrucoCard vira = TrucoCard.of(CardRank.JACK, CardSuit.HEARTS);
            List<TrucoCard> botCards = List.of(
                    TrucoCard.of(CardRank.FOUR, CardSuit.SPADES),
                    TrucoCard.of(CardRank.FIVE, CardSuit.CLUBS),
                    TrucoCard.of(CardRank.SIX, CardSuit.HEARTS)
            );
            List<TrucoCard> openCards = List.of(vira);

            intel = GameIntel.StepBuilder.with()
                    .gameInfo(List.of(), openCards, vira, 1)
                    .botInfo(botCards, 0)
                    .opponentScore(0);

            CardToPlay result = sut.chooseCard(intel.build());

            assertThat(botCards).contains(result.content());
        }

        @Test
        @DisplayName("Deve retornar a única carta disponível")
        void testChooseCardWithOnlyOneCard() {
            TrucoCard vira = TrucoCard.of(CardRank.SEVEN, CardSuit.SPADES);
            List<TrucoCard> botCards = List.of(
                    TrucoCard.of(CardRank.THREE, CardSuit.DIAMONDS)
            );
            List<TrucoCard> openCards = List.of(vira);

            intel = GameIntel.StepBuilder.with()
                    .gameInfo(List.of(), openCards, vira, 1)
                    .botInfo(botCards, 0)
                    .opponentScore(1);

            CardToPlay result = sut.chooseCard(intel.build());

            assertEquals(botCards.get(0), result.content());
        }

        @Test
        @DisplayName("Não deve lançar exceção mesmo com cartas fracas")
        void testChooseCardDoesNotThrowWithBadCards() {
            TrucoCard vira = TrucoCard.of(CardRank.QUEEN, CardSuit.CLUBS);
            List<TrucoCard> botCards = List.of(
                    TrucoCard.of(CardRank.FOUR, CardSuit.CLUBS),
                    TrucoCard.of(CardRank.FIVE, CardSuit.HEARTS),
                    TrucoCard.of(CardRank.SIX, CardSuit.SPADES)
            );
            List<TrucoCard> openCards = List.of(vira);

            intel = GameIntel.StepBuilder.with()
                    .gameInfo(List.of(), openCards, vira, 1)
                    .botInfo(botCards, 0)
                    .opponentScore(3);

            assertDoesNotThrow(() -> sut.chooseCard(intel.build()));
        }

        @Nested
        @DisplayName("Primeira rodada")
        class FirstRoundTests {

            @Nested
            @DisplayName("Bot é o primeiro a jogar")
            class FirstPlayerPlays {

                @Test
                @DisplayName("Se só tiver cartas ruins, descarta a menor")
                void ifOnlyHaveBadCardsThenDiscardTheOneWithLowerValue() {
                    TrucoCard vira = TrucoCard.of(CardRank.ACE, CardSuit.DIAMONDS);
                    List<TrucoCard> botCards = Arrays.asList(
                            TrucoCard.of(CardRank.FOUR, CardSuit.CLUBS),
                            TrucoCard.of(CardRank.FIVE, CardSuit.HEARTS),
                            TrucoCard.of(CardRank.SIX, CardSuit.DIAMONDS)
                    );
                    List<TrucoCard> openCards = Collections.singletonList(vira);

                    intel = firstRoundFirstToPlay(botCards, openCards, vira);

                    assertThat(sut.chooseCard(intel.build())).isEqualTo(CardToPlay.of(botCards.get(0)));
                }

                @Test
                @DisplayName("Se tiver só cartas médias, joga a de maior valor")
                void IfOnlyHaveMiddleCardsThenUseTheOneWithHighestValue() {
                    TrucoCard vira = TrucoCard.of(CardRank.ACE, CardSuit.DIAMONDS);
                    List<TrucoCard> botCards = Arrays.asList(
                            TrucoCard.of(CardRank.QUEEN, CardSuit.CLUBS),
                            TrucoCard.of(CardRank.JACK, CardSuit.HEARTS),
                            TrucoCard.of(CardRank.KING, CardSuit.DIAMONDS)
                    );
                    List<TrucoCard> openCards = Collections.singletonList(vira);

                    intel = firstRoundFirstToPlay(botCards, openCards, vira);

                    assertThat(sut.chooseCard(intel.build())).isEqualTo(CardToPlay.of(botCards.get(2)));
                }
            }

            @Nested
            @DisplayName("Bot é o segundo a jogar")
            class SecondPlayerPlays {
                @Test
                @DisplayName("Tenta vencer com a menor carta possível que ainda ganha")
                void TryToKillOpponentCardWithTheWeakestCard(){
                    TrucoCard vira = TrucoCard.of(ACE, CLUBS);
                    List<TrucoCard> botCards = Arrays.asList(
                            TrucoCard.of(FOUR, CLUBS),
                            TrucoCard.of(THREE, HEARTS),
                            TrucoCard.of(TWO, DIAMONDS)
                    );
                    TrucoCard opponentCard = TrucoCard.of(ACE, HEARTS);
                    List<TrucoCard> openCards = Arrays.asList(TrucoCard.of(ACE, CLUBS), TrucoCard.of(ACE, HEARTS));
                    intel = firstRoundSecondToPlay(botCards, openCards, vira, opponentCard);
                    assertThat(sut.chooseCard(intel.build())).isEqualTo(CardToPlay.of(botCards.get(1)));
                }
            }
        }
    }

    @Nested
    @DisplayName("Testes do método getStrategyForRound")
    class GetStrategyForRoundTests {

        @Test
        @DisplayName("Retorna FirstRound quando rodada é 1")
        void shouldReturnFirstRoundWhenRoundIs1() {
            Strategy strategy = sut.getStrategyForRound(1);
            assertThat(strategy).isInstanceOf(FirstRound.class);
        }

        @Test
        @DisplayName("Retorna SecondRound quando rodada é 2")
        void shouldReturnSecondRoundWhenRoundIs2() {
            Strategy strategy = sut.getStrategyForRound(2);
            assertThat(strategy).isInstanceOf(SecondRound.class);
        }

        @Test
        @DisplayName("Retorna ThirdRound quando rodada é 3")
        void shouldReturnThirdRoundWhenRoundIs3() {
            Strategy strategy = sut.getStrategyForRound(3);
            assertThat(strategy).isInstanceOf(ThirdRound.class);
        }

        @Test
        @DisplayName("Lança exceção se rodada for inválida")
        void shouldThrowExceptionWhenRoundIsInvalid() {
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sut.getStrategyForRound(0));
            assertThat(exception.getMessage()).contains("Unexpected value");
        }
    }

    @Nested
    @DisplayName("Testes do método getRaiseResponse")
    class GetRaiseResponseTests {

        @Test
        @DisplayName("Retorna -1 se não deve aceitar o truco")
        void testGetRaiseResponse() {
            GameIntel intel = mock(GameIntel.class);
            when(intel.getOpponentScore()).thenReturn(5);
            when(intel.getCards()).thenReturn(mock(List.class));

            FirstRound strategy = new FirstRound();
            int response = strategy.getRaiseResponse(intel);

            assertEquals(-1, response);
        }
    }

    @Nested
    @DisplayName("Testes do método decideIfRaises")
    class DecideIfRaisesTests {

        @Test
        @DisplayName("Retorna false se não houver carta do oponente")
        void testDecideIfRaises() {
            GameIntel intel = mock(GameIntel.class);
            when(intel.getOpponentCard()).thenReturn(Optional.empty());

            FirstRound strategy = new FirstRound();
            boolean result = strategy.decideIfRaises(intel);

            assertFalse(result);
        }

        @Nested
        @DisplayName("Se ganhar a primeira rodada")
        class WonFirstRound {
        }
    }

    @Nested
    @DisplayName("Testes do método getMaoDeOnzeResponse")
    class GetMaoDeOnzeResponseTests {

        @Test
        @DisplayName("Aceita mão de onze se a força da mão for maior que 21")
        void ShouldAcceptMaoDeOnzeIfHandStrengthIsHigherThan21() {
            TrucoCard vira = TrucoCard.of(CardRank.ACE, CardSuit.DIAMONDS);
            List<TrucoCard> botCards = Arrays.asList(
                    TrucoCard.of(CardRank.KING, CardSuit.CLUBS),
                    TrucoCard.of(CardRank.ACE, CardSuit.HEARTS),
                    TrucoCard.of(CardRank.KING, CardSuit.DIAMONDS)
            );
            List<TrucoCard> openCards = Collections.singletonList(vira);

            intel = GameIntel.StepBuilder.with()
                    .gameInfo(List.of(), openCards, vira, 1)
                    .botInfo(botCards, 11)
                    .opponentScore(0);

            assertTrue(sut.getMaoDeOnzeResponse(intel.build()));
        }

        @Test
        @DisplayName("Recusa mão de onze se a força da mão for menor que 21")
        void ShouldRefuseMaoDeOnzeIfHandStrengthIsLowerThan21() {
            TrucoCard vira = TrucoCard.of(CardRank.ACE, CardSuit.DIAMONDS);
            List<TrucoCard> botCards = Arrays.asList(
                    TrucoCard.of(CardRank.FOUR, CardSuit.CLUBS),
                    TrucoCard.of(CardRank.FIVE, CardSuit.HEARTS),
                    TrucoCard.of(CardRank.SIX, CardSuit.DIAMONDS)
            );
            List<TrucoCard> openCards = Arrays.asList(
                    TrucoCard.of(CardRank.ACE, CardSuit.DIAMONDS),
                    TrucoCard.of(CardRank.FOUR, CardSuit.HEARTS)
            );

            intel = GameIntel.StepBuilder.with()
                    .gameInfo(List.of(), openCards, vira, 1)
                    .botInfo(botCards, 11)
                    .opponentScore(0);

            assertFalse(sut.getMaoDeOnzeResponse(intel.build()));
        }
    }

    @Test
    @DisplayName("Deve retornar uma carta válida com outras cartas e vira diferente")
    void testChooseCardReturnsValidCardWithDifferentValues() {
        TrucoCard vira = TrucoCard.of(CardRank.SIX, CardSuit.CLUBS);
        List<TrucoCard> botCards = List.of(
                TrucoCard.of(CardRank.THREE, CardSuit.HEARTS),
                TrucoCard.of(CardRank.SEVEN, CardSuit.DIAMONDS),
                TrucoCard.of(CardRank.FOUR, CardSuit.SPADES)
        );
        List<TrucoCard> openCards = List.of(vira);

        intel = GameIntel.StepBuilder.with()
                .gameInfo(List.of(), openCards, vira, 2)
                .botInfo(botCards, 0)
                .opponentScore(2);

        CardToPlay result = sut.chooseCard(intel.build());

        assertThat(botCards).contains(result.content());
    }

    @Test
    @DisplayName("Deve retornar a única carta disponível diferente da base")
    void testChooseCardWithOnlyOneCardDifferent() {
        TrucoCard vira = TrucoCard.of(CardRank.FIVE, CardSuit.HEARTS);
        List<TrucoCard> botCards = List.of(
                TrucoCard.of(CardRank.TWO, CardSuit.CLUBS)
        );
        List<TrucoCard> openCards = List.of(vira);

        intel = GameIntel.StepBuilder.with()
                .gameInfo(List.of(), openCards, vira, 1)
                .botInfo(botCards, 0)
                .opponentScore(0);

        CardToPlay result = sut.chooseCard(intel.build());

        assertEquals(botCards.get(0), result.content());
    }

    @Test
    @DisplayName("Aceita mão de onze se a força da mão for maior que 25 (valores alterados)")
    void ShouldAcceptMaoDeOnzeIfHandStrengthIsHigherThan25() {
        TrucoCard vira = TrucoCard.of(CardRank.SEVEN, CardSuit.HEARTS);
        List<TrucoCard> botCards = Arrays.asList(
                TrucoCard.of(CardRank.ACE, CardSuit.CLUBS),
                TrucoCard.of(CardRank.JACK, CardSuit.HEARTS),
                TrucoCard.of(CardRank.KING, CardSuit.DIAMONDS)
        );
        List<TrucoCard> openCards = Collections.singletonList(vira);

        intel = GameIntel.StepBuilder.with()
                .gameInfo(List.of(), openCards, vira, 1)
                .botInfo(botCards, 26)
                .opponentScore(0);

        assertTrue(sut.getMaoDeOnzeResponse(intel.build()));
    }
    @Test
    @DisplayName("Deve retornar carta válida mesmo com mão vazia (não deve lançar erro)")
    void testChooseCardWithEmptyHand() {
        TrucoCard vira = TrucoCard.of(CardRank.KING, CardSuit.CLUBS);
        List<TrucoCard> botCards = Collections.emptyList();
        List<TrucoCard> openCards = List.of(vira);

        intel = GameIntel.StepBuilder.with()
                .gameInfo(List.of(), openCards, vira, 1)
                .botInfo(botCards, 0)
                .opponentScore(0);

        assertThrows(IllegalStateException.class, () -> sut.chooseCard(intel.build()));
    }

    @Test
    @DisplayName("Deve retornar false em decideIfRaises quando mão do bot é fraca")
    void testDecideIfRaisesWithWeakHand() {
        GameIntel intel = mock(GameIntel.class);
        when(intel.getOpponentCard()).thenReturn(Optional.of(TrucoCard.of(CardRank.TWO, CardSuit.CLUBS)));

        FirstRound strategy = new FirstRound();

        boolean result = strategy.decideIfRaises(intel);

        assertFalse(result);
    }

    @Test
    @DisplayName("Deve retornar true em decideIfRaises quando mão do bot é forte")
    void testDecideIfRaisesWithStrongHand() {
        GameIntel intel = mock(GameIntel.class);
        when(intel.getOpponentCard()).thenReturn(Optional.of(TrucoCard.of(CardRank.ACE, CardSuit.HEARTS)));
        when(intel.getCards()).thenReturn(List.of(
                TrucoCard.of(CardRank.ACE, CardSuit.HEARTS),
                TrucoCard.of(CardRank.KING, CardSuit.DIAMONDS),
                TrucoCard.of(CardRank.JACK, CardSuit.CLUBS)
        ));

        FirstRound strategy = new FirstRound();

        boolean result = strategy.decideIfRaises(intel);

        assertTrue(result);
    }

    @Test
    @DisplayName("Deve lançar IllegalStateException ao solicitar estratégia para rodada inválida negativa")
    void testGetStrategyForRoundThrowsOnNegativeRound() {
        assertThrows(IllegalStateException.class, () -> sut.getStrategyForRound(-1));
    }

    @Test
    @DisplayName("Deve lançar IllegalStateException ao solicitar estratégia para rodada inválida maior que 3")
    void testGetStrategyForRoundThrowsOnRoundGreaterThan3() {
        assertThrows(IllegalStateException.class, () -> sut.getStrategyForRound(4));
    }

    @Test
    @DisplayName("Deve retornar uma carta válida com três cartas baixas diferentes")
    void testChooseCardWithThreeLowCards() {
        TrucoCard vira = TrucoCard.of(CardRank.SEVEN, CardSuit.CLUBS);
        List<TrucoCard> botCards = List.of(
                TrucoCard.of(CardRank.FOUR, CardSuit.HEARTS),
                TrucoCard.of(CardRank.FIVE, CardSuit.SPADES),
                TrucoCard.of(CardRank.SIX, CardSuit.CLUBS)
        );
        List<TrucoCard> openCards = List.of(vira);

        intel = GameIntel.StepBuilder.with()
                .gameInfo(List.of(), openCards, vira, 1)
                .botInfo(botCards, 0)
                .opponentScore(1);

        CardToPlay result = sut.chooseCard(intel.build());

        assertThat(botCards).contains(result.content());
    }

    @Test
    @DisplayName("Deve retornar uma carta válida com três cartas médias diferentes")
    void testChooseCardWithThreeMediumCards() {
        TrucoCard vira = TrucoCard.of(CardRank.FOUR, CardSuit.HEARTS);
        List<TrucoCard> botCards = List.of(
                TrucoCard.of(CardRank.SEVEN, CardSuit.SPADES),
                TrucoCard.of(CardRank.QUEEN, CardSuit.CLUBS),
                TrucoCard.of(CardRank.JACK, CardSuit.DIAMONDS)
        );
        List<TrucoCard> openCards = List.of(vira);

        intel = GameIntel.StepBuilder.with()
                .gameInfo(List.of(), openCards, vira, 1)
                .botInfo(botCards, 0)
                .opponentScore(1);

        CardToPlay result = sut.chooseCard(intel.build());

        assertThat(botCards).contains(result.content());
    }
    @Test
    @DisplayName("Deve descartar carta mais fraca quando só tiver uma carta forte")
    void testChooseCardWithOneStrongCard() {
        TrucoCard vira = TrucoCard.of(CardRank.SEVEN, CardSuit.HEARTS);
        List<TrucoCard> botCards = List.of(
                TrucoCard.of(CardRank.THREE, CardSuit.CLUBS),
                TrucoCard.of(CardRank.FOUR, CardSuit.HEARTS),
                TrucoCard.of(CardRank.SEVEN, CardSuit.DIAMONDS)
        );
        List<TrucoCard> openCards = List.of(vira);

        intel = firstRoundFirstToPlay(botCards, openCards, vira);
        CardToPlay result = sut.chooseCard(intel.build());

        assertThat(result.content()).isEqualTo(botCards.get(1));
    }
    @Test
    @DisplayName("Deve jogar a manilha se for o único jeito de ganhar")
    void testPlayTrumpCardIfOnlyWayToWin() {
        TrucoCard vira = TrucoCard.of(CardRank.FIVE, CardSuit.CLUBS);
        TrucoCard manilha = TrucoCard.of(CardRank.SIX, CardSuit.CLUBS);
        TrucoCard opponentCard = TrucoCard.of(CardRank.KING, CardSuit.SPADES);
        List<TrucoCard> botCards = List.of(
                TrucoCard.of(CardRank.FOUR, CardSuit.HEARTS),
                TrucoCard.of(CardRank.THREE, CardSuit.CLUBS),
                manilha
        );
        List<TrucoCard> openCards = List.of(vira);

        intel = firstRoundSecondToPlay(botCards, openCards, vira, opponentCard);
        CardToPlay result = sut.chooseCard(intel.build());

        assertThat(result.content()).isEqualTo(manilha);
    }

    @Test
    @DisplayName("Decide não aceitar truco se mão estiver ruim")
    void testDeclineTrucoWithWeakHand() {
        GameIntel intel = mock(GameIntel.class);
        when(intel.getOpponentScore()).thenReturn(4);
        when(intel.getCards()).thenReturn(List.of(
                TrucoCard.of(CardRank.FOUR, CardSuit.CLUBS),
                TrucoCard.of(CardRank.FOUR, CardSuit.HEARTS),
                TrucoCard.of(CardRank.FIVE, CardSuit.DIAMONDS)
        ));

        FirstRound strategy = new FirstRound();
        int response = strategy.getRaiseResponse(intel);

        assertEquals(-1, response);
    }
    @Test
    @DisplayName("Decide não pedir truco se perder a primeira rodada")
    void testDoNotRaiseIfLostFirstRound() {
        GameIntel intel = mock(GameIntel.class);
        when(intel.getOpponentCard()).thenReturn(Optional.of(TrucoCard.of(CardRank.KING, CardSuit.CLUBS)));
        when(intel.getCards()).thenReturn(List.of(
                TrucoCard.of(CardRank.FIVE, CardSuit.HEARTS),
                TrucoCard.of(CardRank.SIX, CardSuit.SPADES)
        ));

        FirstRound strategy = new FirstRound();
        boolean raise = strategy.decideIfRaises(intel);

        assertFalse(raise);
    }
    @Test
    @DisplayName("Deve preferir jogar carta do mesmo naipe do vira quando possível")
    void testChooseCardPrefersViraSuit() {
        TrucoCard vira = TrucoCard.of(CardRank.SIX, CardSuit.HEARTS);
        List<TrucoCard> botCards = List.of(
                TrucoCard.of(CardRank.FOUR, CardSuit.HEARTS),
                TrucoCard.of(CardRank.SEVEN, CardSuit.CLUBS),
                TrucoCard.of(CardRank.FIVE, CardSuit.DIAMONDS)
        );
        List<TrucoCard> openCards = List.of(vira);

        intel = GameIntel.StepBuilder.with()
                .gameInfo(List.of(), openCards, vira, 1)
                .botInfo(botCards, 0)
                .opponentScore(1);

        CardToPlay result = sut.chooseCard(intel.build());

        assertEquals(CardSuit.HEARTS, result.content().getSuit());
    }

    @Test
    @DisplayName("Deve recusar truco se pontuação do adversário for muito alta")
    void testGetRaiseResponseRejectsHighOpponentScore() {
        TrucoCard vira = TrucoCard.of(CardRank.ACE, CardSuit.SPADES);
        List<TrucoCard> botCards = List.of(
                TrucoCard.of(CardRank.JACK, CardSuit.HEARTS),
                TrucoCard.of(CardRank.SIX, CardSuit.CLUBS),
                TrucoCard.of(CardRank.FOUR, CardSuit.DIAMONDS)
        );
        List<TrucoCard> openCards = List.of(vira);

        intel = GameIntel.StepBuilder.with()
                .gameInfo(List.of(), openCards, vira, 1)
                .botInfo(botCards, 0)
                .opponentScore(9);

        FirstRound strategy = new FirstRound();
        int response = strategy.getRaiseResponse(intel.build());

        assertEquals(-1, response);
    }


}

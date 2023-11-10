package us.ihmc.scs2.symbolic.parser.parser;

import us.ihmc.euclid.tools.EuclidCoreIOTools;
import us.ihmc.scs2.symbolic.parser.EquationInput;
import us.ihmc.scs2.symbolic.parser.EquationInput.DoubleVariable;
import us.ihmc.scs2.symbolic.parser.EquationInput.IntegerVariable;
import us.ihmc.scs2.symbolic.parser.parser.EquationToken.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EquationParser
{
   private final EquationAliasManager aliasManager = new EquationAliasManager();

   private final EquationOperationLibrary operationLibrary = new EquationOperationLibrary();

   public EquationParser()
   {
   }

   public EquationAliasManager getAliasManager()
   {
      return aliasManager;
   }

   public EquationOperationLibrary getOperationLibrary()
   {
      return operationLibrary;
   }

   // TODO Need to add a way to add constants like immutable.

   /**
    * Parses the string representing an equation and compiles it into a {@code YoEquation} which can be executed later on.
    *
    * @param stringEquation the equation to parse.
    * @return the parsed equation.
    */
   public List<EquationOperation<?>> parse(String stringEquation)
   {
      return parse(stringEquation, true);
   }

   /**
    * Parses the string representing an equation and compiles it into a {@code YoEquation} which can be executed later on.
    *
    * @param stringEquation the equation to parse.
    * @param assignment     if true, then the equation is assumed to be an assignment to an output variable.
    * @return the parsed equation.
    */
   public List<EquationOperation<?>> parse(String stringEquation, boolean assignment)
   {
      List<EquationToken> tokens = tokenizeEquation(stringEquation);

      if (tokens.size() < 3)
         throw new EquationParseError("Too few tokens");

      EquationToken t0 = tokens.get(0);

      insertFunctionsAndVariables(tokens);

      if (t0.getType() != Type.VARIABLE && t0.getType() != Type.WORD)
      {
         List<EquationOperation<?>> operations = compileTokens(tokens);
         // If there's no output, then this is acceptable; otherwise, it's assumed to be a bug.
         EquationInput variable = tokens.get(0).getVariable();
         if (variable != null && assignment)
            throw new IllegalArgumentException("No assignment to an output variable could be found. Found " + t0);
         return operations;
      }
      else
      {
         return parseAssignment(tokens);
      }
   }

   /**
    * Compiles the right side of an assignment.
    * The left side is assumed to be a variable.
    * <p>
    * The tokens are expected to represent an equation of the form: "A = ..." where A is a variable and is the first token, the second token being the
    * assignment operator.
    * </p>
    *
    * @param tokens the tokens representing the whole equation.
    * @return the parsed equation.
    */
   private List<EquationOperation<?>> parseAssignment(List<EquationToken> tokens)
   {
      EquationToken t0 = tokens.get(0);
      EquationToken t1 = tokens.get(1);

      if (t1.getType() != Type.SYMBOL || t1.getSymbol() != EquationSymbol.ASSIGN)
         throw new EquationParseError("Expected assignment operator next");

      // Parse the right side of the equation
      List<EquationToken> tokensRight = tokens.subList(2, tokens.size());
      List<EquationOperation<?>> operations = compileTokens(tokensRight);

      if (tokensRight.get(tokensRight.size() - 1).getType() != Type.VARIABLE)
         throw new EquationParseError("Something went wrong with parsing the block, the last token should be a variable");

      // copy the results into the output
      EquationInput variableRight = tokensRight.get(0).getVariable();
      EquationInput variableLeft;
      if (t0.getType() == Type.WORD) // The type is not known, so infer it from the right side
         variableLeft = EquationInput.newVariable(variableRight.getType());
      else
         variableLeft = t0.getVariable();
      operations.add(operationLibrary.create(EquationSymbol.ASSIGN, variableLeft, variableRight));
      return operations;
   }

   /**
    * Compiles the right side of an equation.
    *
    * @param tokens the tokens representing the whole equation.
    * @return the list of operations to execute to compute the result of the equation.
    */
   private List<EquationOperation<?>> compileTokens(List<EquationToken> tokens)
   {
      for (EquationToken t : tokens)
      {
         if (t.getType() == Type.WORD)
            throw new EquationParseError("Unknown variable on right side. " + t.getWord());
      }

      List<EquationOperation<?>> operations = new ArrayList<>();
      handleParentheses(tokens, operations);

      if (tokens.size() > 1)
         parseBlockNoParentheses(tokens, operations);

      // see if it needs to be parsed more
      if (tokens.size() != 1)
         throw new RuntimeException("BUG");
      return operations;
   }

   /**
    * Searches for pairs of parentheses and processes blocks inside them.
    * This handles nested parentheses.
    *
    * @param unprocessedTokenStack the list of tokens to process
    * @param operationsToPack      the list of operations to add the new operations to.
    */
   protected void handleParentheses(List<EquationToken> unprocessedTokenStack, List<EquationOperation<?>> operationsToPack)
   {
      // have a list to handle embedded parentheses, e.g. (((((a)))))
      List<Integer> leftIndices = new ArrayList<>();

      // find all of them
      for (int i = 0; i < unprocessedTokenStack.size(); i++)
      {
         EquationToken t = unprocessedTokenStack.get(i);

         if (t.getType() != Type.SYMBOL)
            continue;

         if (t.getSymbol() == EquationSymbol.PAREN_LEFT)
         {
            leftIndices.add(i);
            continue;
         }

         if (t.getSymbol() != EquationSymbol.PAREN_RIGHT)
            continue;

         if (leftIndices.isEmpty())
            throw new EquationParseError(") found with no matching (");

         int leftIndex = leftIndices.remove(leftIndices.size() - 1);

         // remember the element before so the new one can be inserted afterwards
         EquationToken beforeLeft = unprocessedTokenStack.get(leftIndex - 1);

         // Sublist with parentheses
         List<EquationToken> sublist = unprocessedTokenStack.subList(leftIndex, i + 1);
         i -= sublist.size();
         // Remove the parentheses
         sublist.remove(0);
         sublist.remove(sublist.size() - 1);

         // if it is a function before "()" then the "()" indicates it is an input to a function
         if (beforeLeft != null && beforeLeft.getType() == Type.FUNCTION)
         {
            List<EquationToken> inputs = parseParameterCommaBlock(sublist, operationsToPack);
            if (inputs.isEmpty())
               throw new EquationParseError("Empty function input parameters");
            parseFunction(beforeLeft, inputs, unprocessedTokenStack, operationsToPack);
         }
         else
         {
            // if null then it was empty inside
            EquationToken output = parseBlockNoParentheses(sublist, operationsToPack);
            if (sublist.size() != 1 && sublist.get(0) != output)
               throw new EquationParseError("Something went wrong with parsing the block");
         }
      }

      if (!leftIndices.isEmpty())
         throw new EquationParseError("Dangling ( parentheses");
   }

   /**
    * Searches for commas in the set of tokens.
    * Used for inputs to functions.
    * Ignore commas which are inside a "[ ]" block.
    *
    * @return List of output tokens between the commas
    */
   protected List<EquationToken> parseParameterCommaBlock(List<EquationToken> tokens, List<EquationOperation<?>> operationsToPack)
   {
      // Find the start/end indices for each argument
      List<Integer> limitIndices = new ArrayList<>();
      limitIndices.add(-1);
      for (int i = 0; i < tokens.size(); i++)
      {
         EquationToken token = tokens.get(i);
         if (token.getType() == Type.SYMBOL && token.getSymbol() == EquationSymbol.COMMA)
            limitIndices.add(i);
      }
      limitIndices.add(tokens.size());

      List<EquationToken> output = new ArrayList<>();

      for (int i = 0; i < limitIndices.size() - 1; i++)
      {
         int start = limitIndices.get(i) + 1;
         int end = limitIndices.get(i + 1);

         if (start == end)
            throw new EquationParseError("No empty function inputs allowed!");

         output.add(parseBlockNoParentheses(tokens.subList(start, end), operationsToPack));
      }

      // Clear the tokens to mark them as processed
      tokens.clear();
      return output;
   }

   /**
    * Parses a code block with no parentheses and no commas. After it is done there should be a single
    * token left, which is returned.
    */
   protected EquationToken parseBlockNoParentheses(List<EquationToken> tokens, List<EquationOperation<?>> operationsToPack)
   {
      if (tokens.isEmpty())
         throw new EquationParseError("Empty block");

      // process operators depending on their priority
      EquationSymbol[] orderedOperations = {EquationSymbol.POWER, EquationSymbol.DIVIDE, EquationSymbol.TIMES, EquationSymbol.MINUS, EquationSymbol.PLUS};

      for (int symbolIndex = 0; symbolIndex < orderedOperations.length; symbolIndex++)
      {
         EquationSymbol nextSymbol = orderedOperations[symbolIndex];

         for (int tokenIndex = 0; tokenIndex < tokens.size(); tokenIndex++)
         {
            EquationToken curr = tokens.get(tokenIndex);

            if (curr.symbol != nextSymbol)
               continue;

            EquationToken prev = tokenIndex > 0 ? tokens.get(tokenIndex - 1) : null;
            EquationToken next = tokenIndex + 1 < tokens.size() ? tokens.get(tokenIndex + 1) : null;

            if (prev == null || prev.getType() != Type.VARIABLE || next == null || next.getType() != Type.VARIABLE)
               throw new EquationParseError("Unexpected setup: prev (%s), curr (%s), next (%s).".formatted(Objects.toString(prev),
                                                                                                           Objects.toString(curr),
                                                                                                           Objects.toString(next)));

            EquationOperation<?> operation = operationLibrary.create(curr.symbol, prev.getVariable(), next.getVariable());

            operationsToPack.add(operation);

            // replace the symbols with their output
            EquationToken outputToken = EquationToken.newVariableToken(operation.getResult());
            tokens.remove(tokenIndex + 1);
            tokens.set(tokenIndex, outputToken);
            tokens.remove(tokenIndex - 1);
            tokenIndex -= 2; // Tokens are getting removed from the list
         }
      }

      if (tokens.size() > 1)
      {
         System.err.println("Remaining tokens: " + tokens.size());
         System.err.println(EuclidCoreIOTools.getCollectionString("\n\t", "", "\n\t", tokens));
         throw new IllegalStateException("There should only be a single token left");
      }
      return tokens.get(0);
   }

   /**
    * Adds a new operation to the list from the operation and two variables. The inputs are removed
    * from the token list and replaced by their output.
    *
    * @param functionToken    token with the function name
    * @param inputTokens      list of tokens to use for creating the function inputs
    * @param tokens           list of tokens to replace the given name token with the actual function
    *                         output.
    * @param operationsToPack list of operations to add the new operation to
    * @return the token which replaces the function name
    */
   protected EquationToken parseFunction(EquationToken functionToken,
                                         List<EquationToken> inputTokens,
                                         List<EquationToken> tokens,
                                         List<EquationOperation<?>> operationsToPack)
   {
      EquationOperation<?> operation;
      if (inputTokens.size() == 1)
         operation = operationLibrary.create(functionToken.getFunctionName(), inputTokens.get(0).getVariable());
      else
      {
         List<EquationInput> functionInputVariables = new ArrayList<>();
         for (int i = 0; i < inputTokens.size(); i++)
         {
            functionInputVariables.add(inputTokens.get(i).getVariable());
         }
         operation = operationLibrary.create(functionToken.getFunctionName(), functionInputVariables);
      }

      operationsToPack.add(operation);

      // replace the symbols with the function's output
      EquationToken t = EquationToken.newVariableToken(operation.getResult());
      tokens.set(tokens.indexOf(functionToken), t);
      return t;
   }

   /**
    * Used when parsing a number.
    * <p>
    * Initially, the number is assumed to be an integer until we discover a decimal point at which
    * point it becomes a float. A float can be defined with a scientific format "1.0e+10", the exponent
    * has to be an integer.
    * </p>
    */
   private enum NumberType
   {
      /**
       * The number being read is assumed to be an integer, can change to float.
       */
      INTEGER,
      /**
       * A decimal point has been read, the number is a float.
       */
      FLOAT,
      /**
       * An exponent has been read, the number is a float and we're reading the exponent number.
       */
      FLOAT_EXP,
   }

   /**
    * Parses the text string to extract tokens.
    */
   private static List<EquationToken> tokenizeEquation(String equation)
   {
      equation += " ";

      List<EquationToken> tokens = new ArrayList<>();

      for (int i = 0; i < equation.length(); i++)
      {
         char c = equation.charAt(i);

         if (Character.isWhitespace(c))
            continue;

         EquationSymbol next = EquationSymbol.lookupSymbolAtStart(equation.substring(i));

         if (next != null)
         {
            if (!EquationSymbol.isSymbolSupported(next))
               throw new EquationParseError("Symbol (%s) is not yet supported.".formatted(next.symbolString));

            EquationToken last = tokens.isEmpty() ? null : tokens.get(tokens.size() - 1);

            if (last != null && last.getType() == Type.SYMBOL && !EquationSymbol.isSymbolDuoValid(last.symbol, next))
               throw new EquationParseError("Invalid sequence of symbols: (%s) followed by (%s).".formatted(last.symbol.symbolString, next.symbolString));

            boolean isSymbol = true;
            if (next == EquationSymbol.MINUS || next == EquationSymbol.PLUS)
            {
               if (i + 1 < equation.length() && Character.isDigit(equation.charAt(i + 1)) && (last == null || isOperatorLR(last.symbol)))
                  isSymbol = false;
            }

            if (isSymbol)
            {
               tokens.add(EquationToken.newSymbolToken(next));
               i += next.symbolString.length() - 1;
               continue;
            }
         }

         if (next == EquationSymbol.MINUS || next == EquationSymbol.PLUS || Character.isDigit(c))
         {
            int start = i;
            NumberType type = NumberType.INTEGER;

            while (i + 1 < equation.length())
            {
               c = equation.charAt(i + 1);

               if (Character.isWhitespace(c))
                  break;

               if (Character.isDigit(c))
               {
                  i++;
               }
               else if (c == '.')
               {
                  if (type == NumberType.FLOAT)
                     throw new EquationParseError("Can't have 2 decimal points.");
                  if (type == NumberType.FLOAT_EXP)
                     throw new EquationParseError("Float exponent must be an integer.");

                  type = NumberType.FLOAT;
                  i++;
               }
               else if (Character.toLowerCase(c) == 'e')
               {
                  if (type == NumberType.FLOAT_EXP)
                     throw new EquationParseError("Can't have 2 exponent.");

                  type = NumberType.FLOAT_EXP;
                  i++;

                  // +/- Symbol typically follows exponent symbol
                  if (equation.charAt(i + 1) == '-' || equation.charAt(i + 1) == '+')
                     i++;
               }
               else
               {
                  break;
               }
            }

            if (type == NumberType.INTEGER)
               tokens.add(EquationToken.newVariableToken(new IntegerVariable(Integer.parseInt(equation.substring(start, i + 1)))));
            else
               tokens.add(EquationToken.newVariableToken(new DoubleVariable(Double.parseDouble(equation.substring(start, i + 1)))));
            continue;
         }

         if (isLetter(c))
         {
            int start = i;

            while (i + 1 < equation.length())
            {
               c = equation.charAt(i + 1);

               if (isLetter(c))
                  i++;
               else
                  break;
            }

            tokens.add(EquationToken.newWordToken(equation.substring(start, i + 1)));
            continue;
         }

         throw new EquationParseError("Unexpected char (%c) at position (%d) in equation: %s".formatted(c, i, equation));
      }

      return tokens;
   }

   /**
    * Search for WORDS in the token list. Then see if the WORD is a function or a variable. If so
    * replace the work with the function/variable
    */
   private void insertFunctionsAndVariables(List<EquationToken> tokens)
   {
      for (int i = 0; i < tokens.size(); i++)
      {
         EquationToken token = tokens.get(i);

         if (token.getType() == Type.WORD)
         {
            EquationInput v = aliasManager.getAlias(token.word);
            if (v != null)
               tokens.set(i, EquationToken.newVariableToken(v));
            else if (operationLibrary.isFunctionName(token.word))
               tokens.set(i, EquationToken.newFunctionToken(token.word));
         }
      }
   }

   /**
    * Operators which affect the variables to its left and right
    */
   private static boolean isOperatorLR(EquationSymbol s)
   {
      if (s == null)
         return false;

      return switch (s)
      {
         case DIVIDE, TIMES, POWER, PLUS, MINUS, ASSIGN -> true;
         default -> false;
      };
   }

   /**
    * Returns true if the character is a valid letter for use in a variable name
    */
   private static boolean isLetter(char c)
   {
      return !(EquationSymbol.isSymbol(c) || Character.isWhitespace(c));
   }

   /**
    * Returns true if the specified name is NOT allowed. It isn't allowed if it matches a built-in
    * operator or if it contains a restricted character.
    */
   private boolean isReserved(String name)
   {
      if (operationLibrary.isFunctionName(name))
         return true;

      for (int i = 0; i < name.length(); i++)
      {
         if (!isLetter(name.charAt(i)))
            return true;
      }
      return false;
   }
}

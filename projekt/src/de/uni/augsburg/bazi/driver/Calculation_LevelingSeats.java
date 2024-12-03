package de.uni.augsburg.bazi.driver;

import de.uni.augsburg.bazi.Resource;
import de.uni.augsburg.bazi.lib.Divisor;
import de.uni.augsburg.bazi.lib.Weight;

public class Calculation_LevelingSeats 
{
	/** Eingabedaten mit mehreren Distrikten */
	private DistrictInputData did;
	
	/**Für Ausgabeformatierung (***) */
	static boolean comment = false;
	
	public String calculate(AbstractInputData aid)
	{
		String output = "";
		
		if (aid instanceof DistrictInputData)
		{
			if (aid.outputFormat.condition == OutputFormat.CONDITION_MIN_VPV)
				output = "Noch nicht implementiert...";
			
			else if(aid.outputFormat.condition == OutputFormat.CONDITION_MIN_PLUS) 
			{
				comment = false;
				did = (DistrictInputData) aid;
				did.prepare();
				
				output += Calculation.getOutputBoundary() + Calculation.COMMENT + aid.title; //+ "\n";	
				for(InputData id : did.data)
					output += calc(id) + "\n";
				output += new CalculationSeparate(did).getCalc();
				output += Calculation.APPENDIX + "\n" + Calculation.getOutputBoundary();
			}
		}
		else if(aid instanceof InputData) 
		{
			comment = true;
			InputData id = (InputData) aid;
			output = calc(id);
		}
		return output;
	}

	
	public String calc(InputData id) 
	{
		String out = "";
		resize(id, 1);
	
		// id.outputFormat.condition = OutputFormat.CONDITION_MIN;
		int[] accuracies = id.accuracies;
		MethodData[] methods = id.outputFormat.methods;

		for (int acc = 0; acc < accuracies.length; acc++)
		{
			if (id.outputFormat.condition == OutputFormat.CONDITION_MIN_PLUS) 
				for (ListInputData lid : id.listData) {
					id.originalWeights[lid.parentIndex].min = (int) Math.round(id.originalWeights[lid.parentIndex].min * (1 + id.minPlusValue / 100));
				}

			if (id.outputFormat.condition == OutputFormat.CONDITION_MIN_VPV)
			{
				id.outputFormat.labelCondition = Resource.getString("bazi.gui.table.min_vpv").replace("h", accuracies[acc] + "");

				ListInputData listOf[] = new ListInputData[id.originalWeights.length];
				for (ListInputData l : id.listData)
					listOf[l.parentIndex] = l;

				double sum = 0;
				for (Weight w : id.originalWeights)
					sum += w.weight;
				double Q = sum / accuracies[acc];

				for (int i = 0; i < id.originalWeights.length; i++)
				{
					Weight w = id.originalWeights[i];
					if (listOf[i] != null)
					{
						w.min = 0;
						for (Weight ws : listOf[i].originalWeights)
							w.min += Math.max(ws.min, (int) (ws.weight / Q));
					}
					else
						w.min = Math.max(w.min, (int) (w.weight / Q));
				}
			}
			
			for (int i = 0; i < id.originalWeights.length; i++) 
			{
				id.weights[0][0][0][i] = id.originalWeights[i].clonew();
			}

			for (int met = 0; met < methods.length; met++)
			{
				int accuracy = accuracies[acc] - 1;
				id.outputFormat.methods = new MethodData[] { methods[met] };				

				boolean condition;
				do
				{
					accuracy++;
					id.accuracies = new int[] { accuracy };
					Calculation calc = new Calculation(id);
					calc.calculate2();

					condition = false;
					int sumOfSeats = 0;
					for (int i = 0; i < id.weights[0][0][0].length; i++)
					{
						Weight w = id.weights[0][0][0][i];
						sumOfSeats += w.rdWeight;
						if (w.conditionEffective)
						{
							condition = true;
							break;
						}
					}
					if (sumOfSeats != accuracy)
						condition = true;
				}
				while (condition);

				if (accuracy == accuracies[acc]) 
					id.accuracies = new int[] { accuracy };
				else if (accuracy == accuracies[acc] + 1) 
					id.accuracies = new int[] { accuracies[acc], accuracy };
				else 
					id.accuracies = new int[] { accuracies[acc], accuracy - 1, accuracy };
				
				resize(id, id.accuracies.length);
				
				if (acc > 0 && met > 0)
					out += "\n\n";
				out += new Calculation(id).calculate2();
			}
		}
		return out;
	}
	
	private void resize(InputData id, int size)
	{
		id.weights = new Weight[size][1][1][id.originalWeights.length];
		for (int a = 0; a < size; a++) 
			for (int c = 0; c < id.originalWeights.length; c++) 
				id.weights[a][0][0][c] = id.originalWeights[c].clonew();			
		id.powers = new double[size][1][1];
		id.save = new Weight[size][id.originalWeights.length];
		id.divSave = new Divisor[size];

		for (ListInputData lid : id.listData)
		{
			lid.weights = new Weight[size][1][1][lid.originalWeights.length];
			for (int a = 0; a < size; a++) 
				for (int c = 0; c < lid.originalWeights.length; c++) 
					lid.weights[a][0][0][c] = lid.originalWeights[c].clonew();
			lid.powers = new double[size][1][1];
			lid.accuracies = new int[size];
			lid.outputFormat.methods = new MethodData[size];
			lid.save = new Weight[size][lid.originalWeights.length];
			lid.divSave = new Divisor[size];
		}
	}
}
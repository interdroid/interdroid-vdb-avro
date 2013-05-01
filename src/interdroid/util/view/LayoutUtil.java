/*
 * Copyright (c) 2008-2012 Vrije Universiteit, The Netherlands All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the Vrije Universiteit nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS IS''
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package interdroid.util.view;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableRow;


/**
 * Utilities for dealing with LayoutParams.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public final class LayoutUtil {

	/**
	 * Some commonly desired weights for LinearLayouts.
	 * @author nick &lt;palmer@cs.vu.nl&gt;
	 *
	 */
	public static enum LayoutWeight {
		/** Zero weight. */
		Zero (0.0f),
		/** One Quarter Weight. */
		Quarter (0.25f),
		/** One Third Weight. */
		Third (0.333333f),
		/** One Half Weight. */
		Half (0.5f),
		/** Two Thirds Weight. */
		TwoThirds (0.666666f),
		/** Three Quarters Weight. */
		ThreeQuarters(0.75f),
		/** Weight of One. */
		One (1f);

		/**
		 * The weight for this constant.
		 */
		private float mWeight;

		/**
		 * Constructor for a LayoutWeight.
		 * @param weight the weight
		 */
		LayoutWeight(final float weight) {
			mWeight = weight;
		}

		/**
		 * @return the weight as a float.
		 */
		public float getWeight() {
			return mWeight;
		}
	}

	/**
	 * Various layout parameters and methods for setting them on views.
	 * @author nick &lt;palmer@cs.vu.nl&gt;
	 *
	 */
	public static enum LayoutParameters {
		/** FILL x FILL. */
		W_FILL_H_FILL,
		/** WRAP x WRAP. */
		W_WRAP_H_WRAP,
		/** WRAP x FILL. */
		W_WRAP_H_FILL,
		/** FILL x WRAP. */
		W_FILL_H_WRAP;

		/**
		 * Sets weighted parameters for a LinearLayout on a view.
		 * @param params the parameters
		 * @param weight the weight
		 * @param view the view
		 */
		public static void setLinearLayoutParams(final LayoutParameters params,
				final LayoutWeight weight, final View view) {
			setLinearLayoutParams(params, weight.getWeight(), view);
		}

		/**
		 * Sets weighted parameters for a LinearLayout on a view.
		 * @param params the parameters
		 * @param weight the weight
		 * @param view the view
		 */
		public static void setLinearLayoutParams(final LayoutParameters params,
				final float weight, final View view) {

			switch (params) {
			case W_FILL_H_FILL:
				view.setLayoutParams(new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.FILL_PARENT,
						LinearLayout.LayoutParams.FILL_PARENT,
						weight
						));
				break;
			case W_WRAP_H_WRAP:
				view.setLayoutParams(new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT,
						weight
						));
				break;
			case W_WRAP_H_FILL:
				view.setLayoutParams(new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.FILL_PARENT,
						weight
						));
				break;
			case W_FILL_H_WRAP:
				view.setLayoutParams(new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.FILL_PARENT,
						LinearLayout.LayoutParams.WRAP_CONTENT,
						weight
						));
				break;
			default:
				throw new IllegalArgumentException("Unknown LayoutParameters");
			}
		}

		/**
		 * Sets un-weighted parameters for a LinearLayout on a view.
		 * @param params the parameters
		 * @param view the view
		 */
		public static void setLinearLayoutParams(final LayoutParameters params,
				final View view) {

			switch (params) {
			case W_FILL_H_FILL:
				view.setLayoutParams(new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.FILL_PARENT,
						LinearLayout.LayoutParams.FILL_PARENT
						));
				break;
			case W_WRAP_H_WRAP:
				view.setLayoutParams(new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT
						));
				break;
			case W_WRAP_H_FILL:
				view.setLayoutParams(new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.FILL_PARENT
						));
				break;
			case W_FILL_H_WRAP:
				view.setLayoutParams(new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.FILL_PARENT,
						LinearLayout.LayoutParams.WRAP_CONTENT
						));
				break;
			default:
				throw new IllegalArgumentException("Unknown LayoutParameters");
			}

		}

		/**
		 * Sets parameters for a ViewGroup on a view.
		 * @param params the parameters
		 * @param view the view
		 */
		public static void setViewGroupLayoutParams(
				final LayoutParameters params, final View view) {

			switch (params) {
			case W_FILL_H_FILL:
				view.setLayoutParams(new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.FILL_PARENT,
						ViewGroup.LayoutParams.FILL_PARENT
						));
				break;
			case W_WRAP_H_WRAP:
				view.setLayoutParams(new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT
						));
				break;
			case W_WRAP_H_FILL:
				view.setLayoutParams(new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.FILL_PARENT
						));
				break;
			case W_FILL_H_WRAP:
				view.setLayoutParams(new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.FILL_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT
						));
				break;
			default:
				throw new IllegalArgumentException("Unknown LayoutParameters");
			}

		}

		/**
		 * Sets un-weighted parameters for a TableRow on a view.
		 * @param params the parameters
		 * @param view the view
		 */
		public static void setTableRowParams(
				final LayoutParameters params, final View view) {

			switch (params) {
			case W_FILL_H_FILL:
				view.setLayoutParams(new TableRow.LayoutParams(
						TableRow.LayoutParams.FILL_PARENT,
						TableRow.LayoutParams.FILL_PARENT
						));
				break;
			case W_WRAP_H_WRAP:
				view.setLayoutParams(new TableRow.LayoutParams(
						TableRow.LayoutParams.WRAP_CONTENT,
						TableRow.LayoutParams.WRAP_CONTENT
						));
				break;
			case W_WRAP_H_FILL:
				view.setLayoutParams(new TableRow.LayoutParams(
						TableRow.LayoutParams.WRAP_CONTENT,
						TableRow.LayoutParams.FILL_PARENT
						));
				break;
			case W_FILL_H_WRAP:
				view.setLayoutParams(new TableRow.LayoutParams(
						TableRow.LayoutParams.FILL_PARENT,
						TableRow.LayoutParams.WRAP_CONTENT
						));
				break;
			default:
				throw new IllegalArgumentException("Unknown LayoutParameters");
			}
		}

		/**
		 * Sets weighted parameters for a TableRow on a view.
		 * @param params the parameters
		 * @param weight the weight
		 * @param view the view
		 */
		public static void setTableRowParams(final LayoutParameters params,
				final LayoutWeight weight, final View view) {
			setTableRowParams(params, weight.getWeight(), view);
		}

		/**
		 * Sets weighted parameters for a TableRow on a view.
		 * @param params the parameters
		 * @param weight the weight
		 * @param view the view
		 */
		public static void setTableRowParams(final LayoutParameters params,
				final float weight, final View view) {

			switch (params) {
			case W_FILL_H_FILL:
				view.setLayoutParams(new TableRow.LayoutParams(
						TableRow.LayoutParams.FILL_PARENT,
						TableRow.LayoutParams.FILL_PARENT,
						weight
						));
				break;
			case W_WRAP_H_WRAP:
				view.setLayoutParams(new TableRow.LayoutParams(
						TableRow.LayoutParams.WRAP_CONTENT,
						TableRow.LayoutParams.WRAP_CONTENT,
						weight
						));
				break;
			case W_WRAP_H_FILL:
				view.setLayoutParams(new TableRow.LayoutParams(
						TableRow.LayoutParams.WRAP_CONTENT,
						TableRow.LayoutParams.FILL_PARENT,
						weight
						));
				break;
			case W_FILL_H_WRAP:
				view.setLayoutParams(new TableRow.LayoutParams(
						TableRow.LayoutParams.FILL_PARENT,
						TableRow.LayoutParams.WRAP_CONTENT,
						weight
						));
				break;
			default:
				throw new IllegalArgumentException("Unknown LayoutParameters");
			}
		}
	}
}

import { forwardRef, useImperativeHandle, useRef, useState } from "react";
import { flushSync } from "react-dom";

import Signature from "@uiw/react-signature";
import { toPng } from "html-to-image";

import type { SignatureRef } from "@uiw/react-signature";
import type { SignatureDtoResponse } from "~/@types/api";

const RESOLUTION = 10_000;
const WIDTH = 1000;
const HEIGHT = 500;

export interface SignatureExporterRef {
	getImage: (paths: SignatureDtoResponse["paths"]) => Promise<string>;
}

export const SignatureRenderer = forwardRef<SignatureExporterRef>((_, ref) => {
	const signatureRef = useRef<SignatureRef>(null);
	const containerRef = useRef<HTMLDivElement>(null);

	const [renderKey, setRenderKey] = useState(0);
	const [currentPoints, setCurrentPoints] = useState<
		Record<string, [number, number][]>
	>({});

	useImperativeHandle(ref, () => ({
		async getImage(paths: SignatureDtoResponse["paths"]): Promise<string> {
			const denormalized = denormalizePoints(paths);

			// Synchronously force React to remount <Signature /> with new points
			flushSync(() => {
				setCurrentPoints(denormalized);
				setRenderKey((prev) => prev + 1);
			});

			if (!containerRef.current)
				throw new Error("Container element is not mounted.");

			return await toPng(containerRef.current, {
				cacheBust: true,
				style: {
					opacity: "1"
				}
			});
		}
	}));

	return (
		<div
			style={{
				position: "absolute",
				left: "-9999px",
				top: "-9999px",
				pointerEvents: "none"
			}}
		>
			<div
				ref={containerRef}
				style={{
					opacity: 0
				}}
			>
				<Signature
					style={
						{ "--w-signature-background": "transparent" } as React.CSSProperties
					}
					key={renderKey}
					width={WIDTH}
					height={HEIGHT}
					ref={signatureRef}
					defaultPoints={currentPoints}
					readonly
				/>
			</div>
		</div>
	);
});

function denormalizePoints(
	rawPoints: SignatureDtoResponse["paths"]
): Record<string, [number, number][]> {
	return Object.fromEntries(
		Object.entries(rawPoints).map(([instant, path]) => [
			instant,
			path.map(({ x, y }) => [
				((x ?? 0) * WIDTH) / RESOLUTION,
				((y ?? 0) * HEIGHT) / RESOLUTION
			])
		])
	);
}

SignatureRenderer.displayName = "SignatureRenderer";
